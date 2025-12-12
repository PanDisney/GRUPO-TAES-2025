<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class UserStatistic extends Model {
    
    protected $fillable = [
        'user_id',
        'email',
        'total_wins',
        'total_matches',
        'coins_earned',
        'current_coins',
        'win_rate',
    ];

    public function user(): BelongsTo {
        return $this->belongsTo(User::class);
    }
}
