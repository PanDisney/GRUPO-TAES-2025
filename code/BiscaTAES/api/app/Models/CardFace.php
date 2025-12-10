<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use App\Models\User;

class CardFace extends Model
{
    use HasFactory;

    protected $fillable = [
        'name',
        'cost',
        'image_name',
    ];

    public function users()
    {
        return $this->belongsToMany(User::class, 'user_card_face', 'card_face_id', 'user_id');
    }
}
