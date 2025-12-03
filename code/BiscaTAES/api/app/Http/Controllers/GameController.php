<?php

namespace App\Http\Controllers;

use App\Models\Game;
use App\Models\User;
use Illuminate\Http\Request;
use App\Http\Requests\StoreGameRequest;
use App\Http\Resources\GameResource;
use Illuminate\Support\Facades\Auth;

class GameController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        $user = Auth::user();
        $games = Game::where('player1_id', $user->id)
            ->orWhere('player2_id', $user->id)
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

        $game = Game::create($request->validated());
        return new GameResource($game);
    }

    /**
     * Display the specified resource.
     */
    public function show(Game $game)
    {
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

    public function bestGames()
    {
        $games = Game::whereNotNull('winner_id')
            ->orderBy('ended_at', 'desc')
            ->paginate(15); // Adjust pagination limit as needed
        return GameResource::collection($games);
    }

    public function quitGame(Request $request)
    {
        $request->validate([
            'game_id' => 'required|exists:games,id',
        ]);

        $user = Auth::user();
        $game = Game::findOrFail($request->game_id);

        if ($game->player1_id !== $user->id && $game->player2_id !== $user->id) {
            return response()->json(['message' => 'Unauthorized to quit this game.'], 403);
        }

        $game->status = 'desisted';
        $game->ended_at = now();
        $game->save();

        return new GameResource($game);
    }
}
