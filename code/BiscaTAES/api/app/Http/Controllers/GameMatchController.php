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

        $match->load('player1', 'player2');

        return new GameMatchResource($match);
    }

    public function update(UpdateMatchRequest $request, GameMatch $match)
    {
        $validatedData = $request->validated();

        if (isset($validatedData['status']) && $validatedData['status'] === 'E') {
            if ($match->began_at) {
                $validatedData['total_time'] = $match->began_at->diffInSeconds(now());
            }

            if ($validatedData['player1_marks'] > $validatedData['player2_marks']) {
                $validatedData['winner_user_id'] = $match->player1_user_id;
            } elseif ($validatedData['player2_marks'] > $validatedData['player1_marks']) {
                $validatedData['winner_user_id'] = $match->player2_user_id;
            }

            $match->games()
                ->whereIn('status', ['PE', 'PL'])
                ->update([
                    'status' => 'I',
                    'ended_at' => now(),
                ]);
        }

        $match->update($validatedData);

        return new GameMatchResource($match);
    }
}
