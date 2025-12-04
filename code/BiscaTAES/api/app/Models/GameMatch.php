<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\Relations\HasOne;

class GameMatch extends Model
{
    protected $table = 'matches';
    public $timestamps = false;

    protected $fillable = [
        'type',
        'player1_user_id',
        'player2_user_id',
        'winner_user_id',
        'loser_user_id',
        'status',
        'stake',
        'began_at',
        'ended_at',
        'total_time',
        'player1_marks',
        'player2_marks',
        'player1_points',
        'player2_points',
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

    public function loser(): HasOne
    {
        return $this->hasOne(User::class, 'id', 'loser_user_id');
    }

    public function games(): HasMany
    {
        return $this->hasMany(Game::class, 'match_id', 'id');
    }
}
