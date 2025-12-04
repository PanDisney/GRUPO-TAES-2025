<?php

namespace App\Http\Controllers;

use App\Models\GameMatch;
use App\Models\User;
use App\Http\Resources\GameMatchResource;
use App\Http\Requests\UpdateMatchRequest;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class GameMatchController extends Controller
{
    public function index()
    {
        $user = Auth::user();
        $matches = GameMatch::where('player1_user_id', $user->id)
            ->orWhere('player2_user_id', $user->id)
            ->with([
                'player1',
                'player2',
                'winner',
                'games',
                'games.player1',
                'games.player2',
                'games.winner'
            ])
            ->get();

        return GameMatchResource::collection($matches);
    }

    public function show(GameMatch $match)
    {
        $match->load([
            'player1',
            'player2',
            'winner',
            'games',
            'games.player1',
            'games.player2',
            'games.winner'
        ]);

        return new GameMatchResource($match);
    }

    public function store(Request $request)
    {
        $user = Auth::user();
        $bot = User::where('email', 'bot@bisca.pt')->first();

        // For single player, we create a match against the bot
        $match = GameMatch::create([
            'player1_user_id' => $user->id,
            'player2_user_id' => $bot->id,
            'status' => 'PL', // Playing
            'type' => 'S',   // Single Player
        ]);

        // Create an initial game for the match
        $match->games()->create([
            'player1_user_id' => $user->id,
            'player2_user_id' => $bot->id,
            'type' => 'S', // Single Player
            'status' => 'PL', // Playing
            'began_at' => now(),
            'player1_points' => 0,
            'player2_points' => 0,
            'player1_moves' => [],
            'player2_moves' => [],
        ]);

        $match->load('player1', 'player2');

        return new GameMatchResource($match);
    }

    public function update(UpdateMatchRequest $request, GameMatch $match)
    {
        $validatedData = $request->validated();
        $match->update($validatedData);

        // If the match status is set to 'E' (Ended), update all associated games
        if (isset($validatedData['status']) && $validatedData['status'] === 'E') {
            $match->games()
                ->whereIn('status', ['PE', 'PL']) // Only update pending or playing games
                ->update([
                    'status' => 'I', // Mark as Interrupted
                    'ended_at' => now(),
                ]);
        }

        return new GameMatchResource($match);
    }
}
