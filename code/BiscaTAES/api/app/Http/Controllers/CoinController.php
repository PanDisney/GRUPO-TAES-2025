<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Transaction;
use App\Models\CoinPurchase;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Http;
use Throwable;

class CoinController extends Controller
{
    public function getCoins(Request $request)
    {
        $user = $request->user();
        return response()->json(['coins' => $user->coins_balance]);
    }

    public function getTransactions(Request $request)
    {
        $user = $request->user();
        $transactions = $user->transactions()->orderBy('created_at', 'desc')->get();
        return response()->json($transactions);
    }

    public function purchaseCoins(Request $request)
    {
        $request->validate([
            'amount' => 'required|integer|min:1',
            'payment_type' => 'required|string|in:MBWAY,PAYPAL,IBAN,MB,VISA',
            'payment_reference' => 'required|string',
        ]);

        $user = $request->user();
        $euros = $request->input('amount');
        $paymentType = $request->input('payment_type');
        $paymentReference = $request->input('payment_reference');
        $coins = $euros * 10;

        try {
            DB::transaction(function () use ($user, $euros, $coins, $paymentType, $paymentReference) {
                // Call external payment gateway
                $response = Http::post('https://dad-payments-api.vercel.app/api/debit', [
                    'type' => $paymentType,
                    'reference' => $paymentReference,
                    'value' => $euros,
                ]);

                if (!$response->successful()) {
                    // Payment failed, throw an exception to rollback the transaction
                    throw new \Exception('Payment failed: ' . $response->body());
                }

                // Create a new transaction
                $transaction = Transaction::create([
                    'user_id' => $user->id,
                    'coin_transaction_type_id' => 2, // 2 is for purchases
                    'coins' => $coins,
                    'transaction_datetime' => now(),
                ]);

                // Create a new coin purchase record
                CoinPurchase::create([
                    'purchase_datetime' => $transaction->transaction_datetime,
                    'user_id' => $user->id,
                    'coin_transaction_id' => $transaction->id,
                    'euros' => $euros,
                    'payment_type' => $paymentType,
                    'payment_reference' => $paymentReference,
                ]);

                // Update user's coin balance
                $user->coins_balance += $coins;
                $user->save();
            });
        } catch (Throwable $e) {
            return response()->json(['message' => 'An error occurred during the purchase.', 'error' => $e->getMessage()], 500);
        }

        return response()->json(['message' => 'Coins purchased successfully', 'coins' => $user->coins_balance]);
    }

    public function deductCoins(Request $request)
        {
            $request->validate([
                'amount' => 'required|integer|min:1',
            ]);

            $user = $request->user();
            $coinsToDeduct = $request->input('amount');

            if ($user->coins_balance < $coinsToDeduct) {
                return response()->json(['message' => 'Insufficient coin balance'], 400);
            }

            $user->coins_balance -= $coinsToDeduct;
            $user->save();

            Transaction::create([
                'user_id' => $user->id,
                'type' => 'deduction',
                'amount' => $coinsToDeduct,
            ]);

            return response()->json(['message' => 'Coins deducted successfully', 'coins' => $user->coins_balance]);
        }
}

