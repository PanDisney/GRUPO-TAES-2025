<?php

namespace App\Services;

use App\Models\User;
use App\Models\UserStatistic;
use App\Models\GameMatch;

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
        $totalWins = 0;
        $coinsEarned = 0;

        // Count wins and coins
        foreach ($matches as $match) {
            if ($match->winner_user_id === $user->id) {
                $totalWins++;
                
                // Get the points this user got
                $userPoints = $user->id === $match->player1_user_id 
                    ? $match->player1_marks 
                    : $match->player2_marks;

                // Calculate coins: 120 points = 80 coins, 90+ = 40, else = 10
                if ($userPoints == 120) {
                    $coinsEarned += 80;
                } elseif ($userPoints >= 91) {
                    $coinsEarned += 40;
                } else {
                    $coinsEarned += 10;
                }
            }
        }

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
