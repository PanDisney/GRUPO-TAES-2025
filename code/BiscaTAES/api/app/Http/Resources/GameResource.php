<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class GameResource extends JsonResource
{
    public static $wrap = null;

    /**
     * Transform the resource into an array.
     *
     * @return array<string, mixed>
     */
    public function toArray(Request $request): array
    {
        return [
            'id' => $this->id,
            'player1' => new UserResource($this->player1),
            'player2' => new UserResource($this->whenLoaded('player2')),
            'winner' => new UserResource($this->whenLoaded('winner')),
            'type' => $this->type,
            'status' => $this->status,
            'player1_moves' => $this->player1_moves,
            'player2_moves' => $this->player2_moves,
            'total_time' => $this->total_time,
            'player1_points' => $this->player1_points,
            'player2_points' => $this->player2_points,
        ];
    }
}
