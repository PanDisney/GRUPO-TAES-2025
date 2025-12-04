<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\GameController;
use App\Http\Controllers\UserController;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\CoinController;
use App\Http\Controllers\AdminController;
use App\Http\Controllers\GameMatchController;


Route::post('/register', [AuthController::class, 'register']);
Route::post('/login', [AuthController::class, 'login']);

Route::middleware('auth:sanctum')->group(function () {
    Route::get('/user', [UserController::class, 'me']);
    Route::get('/users/me', [UserController::class, 'me']);
    Route::put('/users/me', [UserController::class, 'updateAuthenticatedUser']);
    Route::post('/users/me', [UserController::class, 'updateAuthenticatedUser']);
    Route::post('logout', [AuthController::class, 'logout']);
    Route::get('/user/coins', [CoinController::class, 'getCoins']);
    Route::get('/user/transactions', [CoinController::class, 'getTransactions']);
    Route::post('/coins/deduct', [CoinController::class, 'deductCoins']);
    Route::post('/coins/purchase', [CoinController::class, 'purchaseCoins']);

    Route::get('/matches', [GameMatchController::class, 'index']);
    Route::get('/matches/{match}', [GameMatchController::class, 'show']);
    Route::put('/matches/{match}', [GameMatchController::class, 'update']);
    Route::post('/matches', [GameMatchController::class, 'store']);
    Route::apiResource('games', GameController::class);
    Route::put('/games/{game}/move', [GameController::class, 'makeMove']);
    Route::put('/games/{game}/end', [GameController::class, 'endGame']);
});

Route::middleware(['auth:sanctum', 'admin'])->prefix('admin')->group(function () {
    Route::get('/transactions', [AdminController::class, 'getAllTransactions']);
    Route::get('/users', [AdminController::class, 'index']);
});


Route::get('/metadata', function (Request $request) {

    //abort(500, 'Something went wrong');
    return [
        "name" => "DAD 2025/26 Worksheet API",
        "version" => "0.0.1",
        "entry_fee" => 50
    ];
});

Route::apiResource('users', UserController::class);
