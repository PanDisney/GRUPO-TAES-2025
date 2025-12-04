<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UpdateMatchRequest extends FormRequest
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
            'status' => ['required', \Illuminate\Validation\Rule::in(['PE', 'PL', 'E', 'I'])],
            'winner_user_id' => ['nullable', 'integer', 'exists:users,id'],
            'loser_user_id' => ['nullable', 'integer', 'exists:users,id'],
            'player1_marks' => ['nullable', 'integer'],
            'player2_marks' => ['nullable', 'integer'],
            'player1_points' => ['nullable', 'integer'],
            'player2_points' => ['nullable', 'integer'],
        ];
    }
}
