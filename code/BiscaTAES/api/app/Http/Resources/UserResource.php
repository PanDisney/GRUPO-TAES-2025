<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class UserResource extends JsonResource
{
    /**
     * Transform the resource into an array.
     *
     * @return array<string, mixed>
     */
    public function toArray(Request $request): array
    {
        return [
            'id' => $this->id,
            'name' => $this->name,
            'email' => $this->email,
            'nickname' => $this->nickname,
            'coins_balance' => $this->coins_balance,
            'type' => $this->type,
            'photo_avatar_filename' => $this->photo_avatar_filename,
            'created_at' => $this->created_at,
            'updated_at' => $this->updated_at,
        ];
    }
}
