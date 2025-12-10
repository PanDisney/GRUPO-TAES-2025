<?php

namespace App\Models;

// use Illuminate\Contracts\Auth\MustVerifyEmail;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;
use App\Models\Transaction;
use App\Models\CardFace;
use Carbon\Carbon;
use Illuminate\Support\Facades\DB;



class User extends Authenticatable
{
    /** @use HasFactory<\Database\Factories\UserFactory> */
    use HasApiTokens, HasFactory, Notifiable;

    /**
     * The attributes that are mass assignable.
     *
     * @var list<string>
     */
    protected $fillable = [
        'name',
        'email',
        'password',
        'nickname',
        'coins_balance',
        'type',
        'photo_avatar_filename',
        'selected_card_face_id',
    ];

    /**
     * The attributes that should be hidden for serialization.
     *
     * @var list<string>
     */
    protected $hidden = [
        'password',
        'remember_token',
    ];

    /**
     * Get the attributes that should be cast.
     *
     * @return array<string, string>
     */
    protected function casts(): array
    {
        return [
            'email_verified_at' => 'datetime',
            'password' => 'hashed',
        ];
    }

    /**
     * Get the user's photo avatar filename.
     *
     * @param  string|null  $value
     * @return string
     */
    public function getPhotoAvatarFilenameAttribute($value)
    {
        return $value ?: 'anonymous.png';
    }

    public function transactions()
    {
        return $this->hasMany(Transaction::class);
    }

    public function deductCoins(int $amount, int $coinTransactionTypeId): void
    {
        DB::transaction(function () use ($amount, $coinTransactionTypeId) {
            $user = $this->lockForUpdate()->find($this->id);

            if ($user->coins_balance < $amount) {
                throw new \Exception('Insufficient coin balance');
            }

            $user->coins_balance -= $amount;
            $user->save();

            Transaction::create([
                'user_id' => $user->id,
                'coin_transaction_type_id' => $coinTransactionTypeId,
                'coins' => -$amount, // Negative for deduction
                'transaction_datetime' => Carbon::now(),
            ]);
        });
    }

    public function cardFaces()
    {
        return $this->belongsToMany(CardFace::class, 'user_card_face', 'user_id', 'card_face_id');
    }

    public function selectedCardFace()
    {
        return $this->belongsTo(CardFace::class, 'selected_card_face_id');
    }
}
