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
        $validatedData['player1_id'] = $user->id;

        if ($validatedData['type'] === 'S') {
            $bot = User::where('email', 'bot@bisca.pt')->first();
            if ($bot) {
                $validatedData['player2_id'] = $bot->id;
            } else {
                // This should not happen if the seeder has been run
                // But it's good practice to handle it.
                return response()->json(['error' => 'Bot user not found.'], 500);
            }
        }

        $game = Game::create($validatedData);
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
}
