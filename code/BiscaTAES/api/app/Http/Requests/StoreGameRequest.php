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
        return [
            'player1_id' => [
                'sometimes',
                'integer',
                'exists:users,id',
            ],
            'type' => ['required', Rule::in(['S', 'M'])],
            'status' => ['required', Rule::in(['PE', 'PL', 'E', 'I'])],
            'player2_id' => [
                'nullable',
                'required_if:type,M',
                'exists:users,id',
                'different:player1_id',
            ],
            'player1_moves' => ['nullable', 'integer'],
            'player2_moves' => ['nullable', 'integer'],
            'began_at' => ['nullable', 'date'],
            'ended_at' => ['nullable', 'date'],
            'total_time' => ['nullable', 'integer'],
        ];
    }

    /**
     * Get the validation messages for invalid fields.
     *
     * @return array<string, string>
     */
    public function messages(): array
    {
        return [
            'player1_id.integer' => 'Creator ID must be an integer.',
            'player1_id.exists' => 'The selected player does not exist.',
            'type.required' => 'Game type is required.',
            'type.in' => 'Game type must be either S (Single Player) or M (Multiplayer).',
            'status.required' => 'Game status is required.',
            'status.in' => 'Game status must be on of: PE - PEnding , PL - PLaying, E - Ended, I - Interrupted ',
            'player2_id.required_if' => 'Player 2 is required for multiplayer games.',
            'player2_id.exists' => 'The selected player does not exist.',
            'player2_id.different' => 'Player 2 must be different from the creator.',
        ];
    }
}
