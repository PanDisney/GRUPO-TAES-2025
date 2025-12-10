<?php

namespace App\Http\Controllers;

use App\Models\CardFace;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class CardFaceController extends Controller
{
    public function index(Request $request)
    {
        $user = $request->user();
        $cardFaces = CardFace::all();

        $cardFaces->each(function ($cardFace) use ($user) {
            $cardFace->is_owned = $user->cardFaces()->where('card_face_id', $cardFace->id)->exists();
        });

        return response()->json(['card_faces' => $cardFaces]);
    }

    public function purchase(Request $request)
    {
        $request->validate([
            'card_face_id' => 'required|exists:card_faces,id',
        ]);

        $cardFace = CardFace::findOrFail($request->card_face_id);
        $user = Auth::user();

        if ($user->cardFaces()->where('card_face_id', $cardFace->id)->exists()) {
            return response()->json(['message' => 'You already own this card face.'], 400);
        }

        if ($user->coins_balance < $cardFace->cost) {
            return response()->json(['message' => 'Insufficient coins to purchase this card face.'], 400);
        }

        try {
            $user->deductCoins($cardFace->cost, 7); // 7 is the ID for 'Card Face Purchase'
            $user->cardFaces()->attach($cardFace);

            return response()->json(['message' => 'Card face purchased successfully.']);
        } catch (\Exception $e) {
            return response()->json(['message' => $e->getMessage()], 500);
        }
    }
}
