<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasOne;

class Game extends Model
{
    protected $fillable = [
        'player1_id',
        'player2_id',
        'winner_id',
        'type',
        'status',
        'began_at',
        'ended_at',
        'total_time',
        'player1_moves',
        'player2_moves',
    ];

    public function player1(): HasOne
    {
        return $this->hasOne(User::class, 'id', 'player1_id');
    }
}
