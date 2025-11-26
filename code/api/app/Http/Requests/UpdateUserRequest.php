<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;
use Illuminate\Validation\Rules\Password;

class UpdateUserRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        $userId = $this->route('user') ? $this->route('user')->id : $this->user()->id;

        return [
            'name' => ['sometimes', 'string', 'max:255'],
            'email' => [
                'sometimes',
                'string',
                'email',
                'max:255',
                Rule::unique('users')->ignore($userId)
            ],
            'password' => ['sometimes', 'string', Password::min(3)->mixedCase()->numbers()->symbols(), 'confirmed'],
            'nickname' => [
                'sometimes',
                'string',
                'max:255',
                Rule::unique('users')->ignore($userId)
            ],
            'photo_avatar_filename' => ['nullable', 'string', 'max:255'],
        ];
    }
}
