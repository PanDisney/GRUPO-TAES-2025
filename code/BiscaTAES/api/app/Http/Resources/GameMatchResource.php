<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class GameMatchResource extends JsonResource
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
            'type' => $this->type,
            'status' => $this->status,
            'player1' => new UserResource($this->whenLoaded('player1')),
            'player2' => new UserResource($this->whenLoaded('player2')),
            'winner' => new UserResource($this->whenLoaded('winner')),
            'games' => GameResource::collection($this->whenLoaded('games')),
        ];
    }
}
