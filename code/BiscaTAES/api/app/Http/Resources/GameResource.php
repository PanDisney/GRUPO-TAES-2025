<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class GameResource extends JsonResource
{
    /**
     * Transform the resource into an array.
     *
     * @return array<string, mixed>
     */
    public function toArray(Request $request): array
    {
        return [
            'player1' => new UserResource($this->player1),
            'type' => $this->type,
            'status' => $this->status,
            'player1_moves' => $this->player1_moves,
            'total_time' => $this->total_time,
        ];
    }
}
