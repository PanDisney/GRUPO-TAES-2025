<?php

namespace App\Http\Controllers;

use App\Models\UserStatistic;
use Auth;

class RankingController extends Controller {
    
    public function globalRankings() {
        $currentUser = Auth::user();

        // Get all stats sorted by wins
        $allStats = UserStatistic::orderByDesc('total_wins')
            ->orderByDesc('current_coins')
            ->get();

        // Get top 10
        $top10 = $allStats->take(10)->map(function($stat, $index) {
            return [
                'rank' => $index + 1,
                'name' => $stat->user?->name ?? 'Unknown',
                'nickname' => $stat->user?->nickname,
                'total_wins' => $stat->total_wins,
                'coins_earned' => $stat->coins_earned,
            ];
        });

        // Find current user's rank
        $userRank = $allStats->search(function($stat) use ($currentUser) {
            return $stat->user_id === $currentUser->id;
        });

        $userRankNumber = $userRank !== false ? $userRank + 1 : null;
        $userStats = $userRank !== false ? $allStats[$userRank] : null;

        return response()->json([
            'top_10' => $top10,
            'current_user' => [
                'rank' => $userRankNumber,
                'name' => $currentUser->name,
                'total_wins' => $userStats?->total_wins ?? 0,
                'coins_earned' => $userStats?->coins_earned ?? 0,
            ]
        ]);
    }

    public function personalStats() {
        $user = Auth::user();
        $stats = UserStatistic::where('user_id', $user->id)->first();

        if (!$stats) {
            return response()->json([
                'matches_played' => 0,
                'wins' => 0,
                'win_rate' => 0.0,
                'current_coins' => $user->coins_balance ?? 0,
                'coins_earned' => 0,
            ]);
        }

        return response()->json([
            'matches_played' => $stats->total_matches,
            'wins' => $stats->total_wins,
            'win_rate' => $stats->win_rate,
            'current_coins' => $stats->current_coins,
            'coins_earned' => $stats->coins_earned,
        ]);
    }
}
