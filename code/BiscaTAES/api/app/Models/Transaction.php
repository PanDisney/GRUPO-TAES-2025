<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Transaction extends Model
{
    protected $table = 'coin_transactions';

    protected $fillable = [
        'user_id',
        'coin_transaction_type_id',
        'coins',
        'transaction_datetime',
    ];

    public $timestamps = false;
}
