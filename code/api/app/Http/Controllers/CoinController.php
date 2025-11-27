<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Transaction;
use Illuminate\Support\Facades\Auth;

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
        ]);

        $user = $request->user();
        $euros = $request->input('amount');
        $coins = $euros * 10;

        $user->coins_balance += $coins;
        $user->save();

        Transaction::create([
            'user_id' => $user->id,
            'type' => 'purchase',
            'amount' => $coins,
        ]);

        return response()->json(['message' => 'Coins purchased successfully', 'coins' => $user->coins_balance]);
    }
}

