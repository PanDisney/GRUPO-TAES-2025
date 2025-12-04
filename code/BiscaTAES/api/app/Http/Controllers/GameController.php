<?php

namespace App\Http\Controllers;

use App\Models\Game;
use App\Models\User;
use Illuminate\Http\Request;
use App\Http\Requests\StoreGameRequest;
use App\Http\Resources\GameResource;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Log;

class GameController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        $user = Auth::user();
        $games = Game::where('player1_user_id', $user->id)
            ->orWhere('player2_user_id', $user->id)
            ->with('player1', 'player2', 'winner')
            ->get();
        return GameResource::collection($games);
    }

    /**
     * Store a newly created resource in storage.
     */
    public function store(StoreGameRequest $request)
    {
        $user = Auth::user();
        $user->deductCoins(50);

        $validatedData = $request->validated();
        $validatedData['player1_user_id'] = $user->id;

        if ($validatedData['type'] === 'S') {
            $bot = User::where('email', 'bot@bisca.pt')->first();
            if ($bot) {
                $validatedData['player2_user_id'] = $bot->id;
            } else {
                // This should not happen if the seeder has been run
                // But it's good practice to handle it.
                return response()->json(['error' => 'Bot user not found.'], 500);
            }
        }

        Log::debug('Data for Game::create', $validatedData);
        $game = Game::create($validatedData);
        $game->load('player1', 'player2');
        return new GameResource($game);
    }

    /**
     * Display the specified resource.
     */
    public function show(Game $game)
    {
        $game->load('player1', 'player2', 'winner');
        return new GameResource($game);
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(StoreGameRequest $request, Game $game)
    {
        $game->update($request->validated());
        return new GameResource($game);
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(Game $game)
    {
        //
    }

    public function makeMove(Request $request, Game $game)
    {
        $user = Auth::user();

        // 1. Authorization: Check if the authenticated user is a player in this game
        if ($game->player1_user_id !== $user->id && $game->player2_user_id !== $user->id) {
            return response()->json(['message' => 'You are not a player in this game.'], 403);
        }

        // 2. Game Status Check: Ensure the game is currently playing
        if ($game->status !== 'PL') {
            return response()->json(['message' => 'This game is not currently playing.'], 400);
        }

        // 3. Validate Move Data: Assuming 'move' is a generic array or object for the move itself
        $request->validate([
            'move' => ['required', 'array'], // or 'object' depending on your move structure
        ]);

        // 4. Determine Player and Append Move
        $moveData = $request->input('move');

        if ($game->player1_user_id === $user->id) {
            // Player 1 is making a move
            $moves = $game->player1_moves ?? []; // Initialize as empty array if null
            $moves[] = $moveData;
            $game->player1_moves = $moves;
        } else {
            // Player 2 is making a move
            $moves = $game->player2_moves ?? []; // Initialize as empty array if null
            $moves[] = $moveData;
            $game->player2_moves = $moves;
        }

        $game->save();
        $game->load('player1', 'player2', 'winner'); // Reload relationships for the resource

        return new GameResource($game);
    }

    public function endGame(Request $request, Game $game)
    {
        $user = Auth::user();

        // 1. Authorization: Check if the authenticated user is a player in this game
        if ($game->player1_user_id !== $user->id && $game->player2_user_id !== $user->id) {
            return response()->json(['message' => 'You are not a player in this game.'], 403);
        }

        // 2. Game Status Check: Ensure the game is currently playing
        if ($game->status !== 'PL') {
            return response()->json(['message' => 'This game is not currently playing.'], 400);
        }

        // 3. Validate Request Data for ending the game
        $validatedData = $request->validate([
            'winner_user_id' => ['nullable', 'integer', 'exists:users,id'],
            'player1_points' => ['nullable', 'integer', 'min:0'],
            'player2_points' => ['nullable', 'integer', 'min:0'],
            'is_draw' => ['nullable', 'boolean'],
        ]);

        // Apply validated data
        $game->status = 'E'; // Mark as Ended
        $game->ended_at = now();

        if (isset($validatedData['winner_user_id'])) {
            $game->winner_user_id = $validatedData['winner_user_id'];
            // Determine loser if winner is provided
            if ($game->winner_user_id === $game->player1_user_id) {
                $game->loser_user_id = $game->player2_user_id;
            } else if ($game->winner_user_id === $game->player2_user_id) {
                $game->loser_user_id = $game->player1_user_id;
            }
        } else if (isset($validatedData['is_draw']) && $validatedData['is_draw'] === true) {
            $game->is_draw = true;
            $game->winner_user_id = null; // Ensure winner is null for a draw
            $game->loser_user_id = null; // Ensure loser is null for a draw
        }

        // Update points if provided, otherwise leave current or default (0)
        if (isset($validatedData['player1_points'])) {
            $game->player1_points = $validatedData['player1_points'];
        }
        if (isset($validatedData['player2_points'])) {
            $game->player2_points = $validatedData['player2_points'];
        }

        $game->save();
        $game->load('player1', 'player2', 'winner', 'loser'); // Reload relationships for the resource

        return new GameResource($game);
    }
}
