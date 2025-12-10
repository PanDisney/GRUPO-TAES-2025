<?php

namespace App\Http\Controllers;

use App\Http\Requests\StoreUserRequest;
use App\Http\Requests\UpdateUserRequest;
use App\Models\User;
use App\Http\Resources\UserResource;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Storage;

class UserController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        return UserResource::collection(User::all());
    }

    /**
     * Store a newly created resource in storage.
     */
    public function store(StoreUserRequest $request)
    {
        $user = User::create($request->validated());
        return new UserResource($user);
    }

    /**
     * Display the specified resource.
     */
    public function show(User $user)
    {
        return new UserResource($user);
    }

    /**
     * Display the authenticated user.
     */
    public function me(Request $request)
    {
        return new UserResource($request->user());
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(UpdateUserRequest $request, User $user)
    {
        $user->update($request->validated());
        return new UserResource($user);
    }

    /**
     * Update the authenticated user's profile.
     */
    public function updateAuthenticatedUser(UpdateUserRequest $request)
    {
        $user = $request->user();
        $dataToUpdate = $request->safe()->except('photo_avatar_filename');

        if (isset($dataToUpdate['password'])) {
            $dataToUpdate['password'] = Hash::make($dataToUpdate['password']);
        }

        if ($request->hasFile('photo_avatar_filename')) {
            // Correct usage of store(): specify path and disk.
            $path = $request->file('photo_avatar_filename')->store('photos_avatars', 'public');
            $dataToUpdate['photo_avatar_filename'] = basename($path);
        }

        $user->update($dataToUpdate);

        return new UserResource($user);
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(User $user)
    {
        $user->delete();
        return response()->noContent();
    }

    public function selectCardFace(Request $request)
    {
        $request->validate([
            'card_face_id' => 'required|exists:card_faces,id',
        ]);

        $user = $request->user();
        $cardFaceId = $request->input('card_face_id');

        if (!$user->cardFaces()->where('card_face_id', $cardFaceId)->exists()) {
            return response()->json(['message' => 'You do not own this card face.'], 400);
        }

        $user->selected_card_face_id = $cardFaceId;
        $user->save();

        return response()->json(['message' => 'Card face selected successfully.']);
    }
}
