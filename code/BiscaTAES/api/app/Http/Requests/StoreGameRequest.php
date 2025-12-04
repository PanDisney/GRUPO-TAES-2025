<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class StoreGameRequest extends FormRequest
{
    /**
     * Determine if the user is authorized to make this request.
     */
    public function authorize(): bool
    {
        return true;
    }

    /**
     * Get the validation rules that apply to the request.
     *
     * @return array<string, \Illuminate\Contracts\Validation\ValidationRule|array<mixed>|string>
     */
    public function rules(): array
    {
        $rules = [
            'status' => ['required', Rule::in(['PE', 'PL', 'E', 'I'])],
            'winner_user_id' => ['nullable', 'integer', 'exists:users,id'],
            'player1_moves' => ['nullable', 'array'],
            'player2_moves' => ['nullable', 'array'],
            'player1_points' => ['nullable', 'integer'],
            'player2_points' => ['nullable', 'integer'],
            'began_at' => ['nullable', 'date'],
            'ended_at' => ['nullable', 'date'],
            'total_time' => ['nullable', 'integer'],
        ];

        if ($this->isMethod('post')) {
            $rules['type'] = ['required', Rule::in(['S', 'M'])];
            // player1_user_id is not required on create because it's set from the authenticated user
            // match_id is required on create to link the game to a match
            $rules['match_id'] = ['required', 'integer', 'exists:matches,id'];
        }

        return $rules;
    }

    /**
     * Get the validation messages for invalid fields.
     *
     * @return array<string, string>
     */
    public function messages(): array
    {
        return [
            'player1_user_id.integer' => 'Creator ID must be an integer.',
            'player1_user_id.exists' => 'The selected player does not exist.',
            'match_id.required' => 'Match ID is required.',
            'match_id.exists' => 'The selected match does not exist.',
            'type.required' => 'Game type is required.',
            'type.in' => 'Game type must be either S (Single Player) or M (Multiplayer).',
            'status.required' => 'Game status is required.',
            'status.in' => 'Game status must be on of: PE - PEnding , PL - PLaying, E - Ended, I - Interrupted ',
            'player2_user_id.required_if' => 'Player 2 is required for multiplayer games.',
            'player2_user_id.exists' => 'The selected player does not exist.',
            'player2_user_id.different' => 'Player 2 must be different from the creator.',
            'player1_points.integer' => 'Player 1 points must be an integer.',
            'player2_points.integer' => 'Player 2 points must be an integer.',
        ];
    }
}
