<?php

namespace App\Services;

use App\Models\User;
use App\Models\UserStatistic;
use App\Models\GameMatch;
use App\Models\Transaction;

class UserStatisticsService {
    
    public static function updateUserStats(User $user): void {
        // Get all matches where user played
        $matches = GameMatch::where(function($q) use ($user) {
            $q->where('player1_user_id', $user->id)
              ->orWhere('player2_user_id', $user->id);
        })
        ->where('status', 'E') // Only ended matches
        ->get();

        $totalMatches = $matches->count();
        $totalWins = $matches->where('winner_user_id', $user->id)->count();

        // Calculate coins earned from match payouts
        $coinsEarned = Transaction::where('user_id', $user->id)
            ->where('coin_transaction_type_id', 6) // ID for 'Match payout'
            ->sum('coins');

        // Calculate win rate
        $winRate = $totalMatches > 0 ? ($totalWins / $totalMatches) * 100 : 0;

        // Save to user_statistics table
        UserStatistic::updateOrCreate(
            ['user_id' => $user->id],
            [
                'email' => $user->email,
                'total_wins' => $totalWins,
                'total_matches' => $totalMatches,
                'coins_earned' => $coinsEarned,
                'current_coins' => $user->coins_balance ?? 0,
                'win_rate' => $winRate,
            ]
        );
    }
}
