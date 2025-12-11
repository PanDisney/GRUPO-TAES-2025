<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasOne;

class Game extends Model
{
    public $timestamps = false;

    protected $fillable = [
        'match_id',
        'player1_user_id',
        'player2_user_id',
        'winner_user_id',
        'is_draw', // Add this
        'type',
        'status',
        'began_at',
        'ended_at',
        'total_time',
        'player1_points',
        'player2_points',
        'player1_moves',
        'player2_moves',
        'trump_card', // Add this
        'first_trick_leader_id', // Add this
    ];

    protected $casts = [
        'is_draw' => 'boolean', // Add this
        'player1_moves' => 'array',
        'player2_moves' => 'array',
    ];

    public function player1(): HasOne
    {
        return $this->hasOne(User::class, 'id', 'player1_user_id');
    }

    public function player2(): HasOne
    {
        return $this->hasOne(User::class, 'id', 'player2_user_id');
    }

    public function winner(): HasOne
    {
        return $this->hasOne(User::class, 'id', 'winner_user_id');
    }

    public function match(): BelongsTo
    {
        return $this->belongsTo(GameMatch::class, 'match_id', 'id');
    }

    public function firstTrickLeader(): HasOne
    {
        return $this->hasOne(User::class, 'id', 'first_trick_leader_id');
    }
}
