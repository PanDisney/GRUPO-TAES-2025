<?php

namespace App\Http\Controllers;

use App\Models\Game;
use App\Models\GameMatch;
use App\Models\User;
use App\Http\Resources\GameMatchResource;
use App\Http\Requests\UpdateMatchRequest;
use App\Services\BiscaGameService;
use App\Services\UserStatisticsService;  // ⭐ ADD THIS LINE
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class GameMatchController extends Controller
{
    private function calculateTricksForGame(Game $game): Game
    {
        $game->load('player1', 'player2', 'winner', 'firstTrickLeader');

        $tricks = [];
        $player1Moves = $game->player1_moves ?? [];
        $player2Moves = $game->player2_moves ?? [];
        $leaderId = $game->first_trick_leader_id;
        $trumpSuit = null;
        if ($game->trump_card) {
            $trumpCard = BiscaGameService::parseCard($game->trump_card);
            $trumpSuit = $trumpCard['suit'] ?? null;
        }

        $numTricks = count($player1Moves);
        for ($i = 0; $i < $numTricks; $i++) {
            $player1Move = $player1Moves[$i];
            $player2Move = $player2Moves[$i];

            $player1Card = BiscaGameService::parseCard($player1Move['card']);
            $player2Card = BiscaGameService::parseCard($player2Move['card']);

            $winnerId = null;
            if ($player1Card && $player2Card && $trumpSuit && $leaderId) {
                $winnerId = BiscaGameService::determineTrickWinner(
                    $player1Card,
                    $player2Card,
                    $trumpSuit,
                    $leaderId,
                    $game->player1_user_id,
                    $game->player2_user_id
                );
            }

            $tricks[] = [
                'trick_number' => $i + 1,
                'leader_id' => $leaderId,
                'player1_card' => $player1Move['card'],
                'player2_card' => $player2Move['card'],
                'winner_id' => $winnerId,
            ];

            if ($winnerId) {
                $leaderId = $winnerId;
            }
        }

        $game->tricks = $tricks;
        return $game;
    }

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

        $matches->each(function ($match) {
            $match->games->each(function ($game) {
                $this->calculateTricksForGame($game);
            });
        });

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

        $match->games->each(function ($game) {
            $this->calculateTricksForGame($game);
        });

        return new GameMatchResource($match);
    }

    public function store(Request $request)
    {
        $user = Auth::user();
        $user->deductCoins(50, 4); // Deduct 50 coins for a 'Match stake' (ID 4)
        $bot = User::where('email', 'bot@bisca.pt')->first();

        // For single player, we create a match against the bot
        $match = GameMatch::create([
            'player1_user_id' => $user->id,
            'player2_user_id' => $bot->id,
            'status' => 'PL', // Playing
            'type' => 'S',   // Single Player
            'began_at' => now(),
        ]);

        $match->load('player1.selectedCardFace', 'player2');

        return new GameMatchResource($match);
    }

    public function update(UpdateMatchRequest $request, GameMatch $match)
    {
        $validatedData = $request->validated();

        if (isset($validatedData['status']) && $validatedData['status'] === 'E') {
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

        // ⭐ UPDATE STATS WHEN MATCH ENDS
        if (isset($validatedData['status']) && $validatedData['status'] === 'E') {
            UserStatisticsService::updateUserStats($match->player1);
            UserStatisticsService::updateUserStats($match->player2);
        }

        return new GameMatchResource($match);
    }
}
