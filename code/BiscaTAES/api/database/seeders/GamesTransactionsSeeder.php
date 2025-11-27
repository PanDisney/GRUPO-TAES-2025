<?php

namespace Database\Seeders;

use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;

class GamesTransactionsSeeder extends Seeder
{
    private $faker = null;
    private $users = null;


    public function run(): void
    {
        $this->command->info("Create game related Transactions");
        $this->faker = \Faker\Factory::create(DatabaseSeeder::$seedLanguage);

        $this->users = DB::table('users')->orderBy('id', 'asc')->get();

        $games = DB::table('games')->whereNull('match_id')->orderBy('began_at', 'asc')->get();
        $matches = DB::table('matches')->orderBy('began_at', 'asc')->get();

        $coin_purchases = [];
        $coin_transactions = [];
        $i = 0;
        while($games->count() > 0 || $matches->count() > 0) {
            $firstGame = $games->first();
            $firstMatch = $matches->first();
            $isGameFirst = false;
            if ($firstGame && $firstMatch) {
                $isGameFirst = $firstGame->began_at < $firstMatch->began_at;
            } elseif ($firstGame && !$firstMatch) {
                $isGameFirst = true;
            } elseif (!$firstGame && $firstMatch) {
                $isGameFirst = false;
            }
            if ($isGameFirst) {
                $this->handleGame($coin_purchases, $coin_transactions, $firstGame);
                $games->shift();
            } else {
                $this->handleMatch($coin_purchases, $coin_transactions, $firstMatch);
                $matches->shift();
            }

            $i++;
            if ($i >= DatabaseSeeder::$dbInsertBlockSize) {
                $this->command->info("Block of coin purchases and transactions");
                if (!empty($coin_transactions)) {
                    DB::table('coin_transactions')->insert($coin_transactions);
                    $this->command->info("Saved " . count($coin_transactions) . " coin transactions");
                }
                if (!empty($coin_purchases)) {
                    DB::table('coin_purchases')->insert($coin_purchases);
                    $this->command->info("Saved " . count($coin_purchases) . " coin purchases");
                }
                $i = 0;
                $coin_purchases = [];
                $coin_transactions = [];
            }
        }
        $this->command->info("Last block of coin purchases and transactions");
        if (!empty($coin_transactions)) {
            DB::table('coin_transactions')->insert($coin_transactions);
            $this->command->info("Saved " . count($coin_transactions) . " coin transactions");
        }
        if (!empty($coin_purchases)) {
            DB::table('coin_purchases')->insert($coin_purchases);
            $this->command->info("Saved " . count($coin_purchases) . " coin purchases");
        }
        $this->updateAllUsersBalance();
        $this->command->info("Game related Transactions Created Successfully.");
    }

    private function getTimeStampFromDB($strDateFromDB, $minusOneSecond = false)
    {
        if ($minusOneSecond) {
            return date("Y-m-d H:i:s", strtotime($strDateFromDB . " - 1 second"));
        }
        return date("Y-m-d H:i:s", strtotime($strDateFromDB));
    }


    private function getRandomPayment($user, &$type, &$ref): void
    {
        $type = $this->faker->randomElement(['MBWAY', 'PAYPAL', 'IBAN', 'MB', 'VISA']);
        switch ($type) {
            case 'MBWAY':
                $ref = rand(910000000, 999999999);
                break;
            case 'PAYPAL':
                $ref = $user->email;
                break;
            case 'IBAN':
                $ref = 'PT' . rand(5000000000000, 9999999999999) . rand(1000000000, 9999999999);
                break;
            case 'MB':
                $ref = rand(10000, 99999) . '-' . rand(100000000, 999999999);
                break;
            case 'VISA':
                $ref = rand(40000000, 49999999) . rand(10000000, 99999999);
                break;
        }
    }

    private $purchaseID = null;
    private function addPurchase(&$coin_purchases, &$coin_transactions, &$user, $time, $eurosAmount)
    {
        $this->purchaseID++;
        $coins = $eurosAmount * 10;

        $newCoinTransaction = $this->addCoinTransaction($coin_transactions, $user, $coins, 2, $time, null, null);

        $this->getRandomPayment($user, $paymentType, $paymentRef);

        $coin_purchases[] = [
            'id' => $this->purchaseID,
            'purchase_datetime' => $time,
            'user_id' => $user->id,
            'coin_transaction_id' => $newCoinTransaction['id'],
            'euros' => $eurosAmount,
            'payment_type' => $paymentType,
            'payment_reference' => $paymentRef,
            'custom' => null,
        ];
    }

    private $transactionID = 0;
    private function addCoinTransaction(&$coin_transactions, &$user, $coins, $type, $time, $game_id = null, $match_id = null)
    {
        if ($this->transactionID == null) {
            $this->transactionID = DB::table('coin_transactions')->max('id');
        }

        // Types of coin transactions:
        // 1 - Bonus
        // 2 - Coin purchase
        // 3 - Game fee - requires game_id
        // 4 - Match stake - requires match_id
        // 5 - Game payout - requires game_id
        // 6 - Match payout - requires match_id
        if(($type == 3 || $type == 5) && $game_id === null) {
            throw new \Exception("Game ID is required for coin transaction type $type");
        }
        if(($type == 4 || $type == 6) && $match_id === null) {
            throw new \Exception("Match ID is required for coin transaction type $type");
        }
        $this->transactionID++;

        $coins = abs($coins);
        if (in_array($type, [3, 4])) {
            $coins *= -1;
        }

        $newCoinTransaction = [
            'id' => $this->transactionID,
            'transaction_datetime' => $time,
            'user_id' => $user->id,
            'match_id' => $match_id, // nullable
            'game_id' => $game_id,   // nullable
            'coin_transaction_type_id' => $type,
            'coins' => $coins,
            'custom' => null,
        ];
        $coin_transactions[] = $newCoinTransaction;
        $user->coins_balance += $coins;
        return $newCoinTransaction;
    }

    private function calculateGamePayouts($game)
    {
        $payouts = [0, 0];
        if ($game->player1_points == $game->player2_points) {
            $payouts = [1, 1];
        } elseif ($game->player1_points == 120) {
            $payouts = [6, 0];
        } elseif ($game->player2_points == 120) {
            $payouts = [0, 6];
        } elseif ($game->player1_points >= 91) {
            $payouts = [4, 0];
        } elseif ($game->player2_points >= 91) {
            $payouts = [0, 4];
        } elseif ($game->player1_points >= 61) {
            $payouts = [3, 0];
        } elseif ($game->player2_points >= 61) {
            $payouts = [0, 3];
        } else { // this condition should never be reached
            $payouts = [0, 0];
        }
        return $payouts;
    }

    private function calculateMatchPayouts($match)
    {
        $payouts = [0, 0];
        if ($match->winner_user_id == $match->player1_user_id) {
            $payouts = [$match->stake * 2 - 1, 0];
        } elseif ($match->winner_user_id == $match->player2_user_id) {
            $payouts = [0, $match->stake * 2 - 1];
        }
        return $payouts;
    }

    private function handleGame(&$coin_purchases, &$coin_transactions, $game)
    {
        $player1 = $this->users->where('id', $game->player1_user_id)->first();
        $player2 = $this->users->where('id', $game->player2_user_id)->first();
        // not enough coins to play - must purchase before the game
        $timeOneSecondBefore = $this->getTimeStampFromDB($game->began_at, true);
        if ($player1->coins_balance < 2) {
            $this->addPurchase($coin_purchases, $coin_transactions, $player1, $timeOneSecondBefore, random_int(1, 10));
        }
        // not enough coins to play - must purchase before the game
        if ($player2->coins_balance < 2) {
            $this->addPurchase($coin_purchases, $coin_transactions, $player2, $timeOneSecondBefore, random_int(1, 10));
        }
        $this->addCoinTransaction($coin_transactions, $player1, -2, 3, $this->getTimeStampFromDB($game->began_at, false), $game->id);
        $this->addCoinTransaction($coin_transactions, $player2, -2, 3, $this->getTimeStampFromDB($game->began_at, false), $game->id);
        $payouts = $this->calculateGamePayouts($game);
        if ($payouts[0] > 0) {
            $this->addCoinTransaction($coin_transactions, $player1, $payouts[0], 5, $this->getTimeStampFromDB($game->ended_at, false), $game->id);
        }
        if ($payouts[1] > 0) {
            $this->addCoinTransaction($coin_transactions, $player2, $payouts[1], 5, $this->getTimeStampFromDB($game->ended_at, false), $game->id);
        }
    }

    private function handleMatch(&$coin_purchases, &$coin_transactions, $match)
    {
        $player1 = $this->users->where('id', $match->player1_user_id)->first();
        $player2 = $this->users->where('id', $match->player2_user_id)->first();
        // not enough coins to play - must purchase before the match
        $timeOneSecondBefore = $this->getTimeStampFromDB($match->began_at, true);
        if ($player1->coins_balance < $match->stake) {
            $requiredCoins = $match->stake - $player1->coins_balance;
            $euros = ceil(($requiredCoins + 3) / 10);
            $this->addPurchase($coin_purchases, $coin_transactions, $player1, $timeOneSecondBefore, $euros);
        }
        // not enough coins to play - must purchase before the match
        if ($player2->coins_balance < $match->stake) {
            $requiredCoins = $match->stake - $player2->coins_balance;
            $euros = ceil(($requiredCoins + 3) / 10);
            $this->addPurchase($coin_purchases, $coin_transactions, $player2, $timeOneSecondBefore, $euros);
        }
        $this->addCoinTransaction($coin_transactions, $player1, $match->stake * -1, 4, $this->getTimeStampFromDB($match->began_at, false), null, $match->id);
        $this->addCoinTransaction($coin_transactions, $player2, $match->stake * -1, 4, $this->getTimeStampFromDB($match->began_at, false), null, $match->id);
        $payouts = $this->calculateMatchPayouts($match);
        if ($payouts[0] > 0) {
            $this->addCoinTransaction($coin_transactions, $player1, $payouts[0], 6, $this->getTimeStampFromDB($match->ended_at, false), null, $match->id);
        }
        if ($payouts[1] > 0) {
            $this->addCoinTransaction($coin_transactions, $player2, $payouts[1], 6, $this->getTimeStampFromDB($match->ended_at, false), null, $match->id);
        }
    }

    private function updateAllUsersBalance()
    {
        $this->command->info("Update users balances...");
        foreach ($this->users as $user) {
            DB::table('users')->where('id', $user->id)->update(['coins_balance' => $user->coins_balance]);
        }
        $this->command->info("All users balances updated.");
    }
}
