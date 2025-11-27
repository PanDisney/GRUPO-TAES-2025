<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            // Administrator, Player
            $table->enum('type', ['A', 'P'])->default('P');

            // Nickname - must be unique
            $table->string('nickname', 20)->nullable()->unique();

            // User access is blocked
            $table->boolean('blocked')->default(false);

            // User Photo/Avatar
            $table->string('photo_avatar_filename')->nullable();

            // Brain Coin Balance
            $table->integer('coins_balance')->default(0);

            // Users can be deleted with "soft deletes"
            $table->softDeletes();

            // custom data
            $table->json('custom')->nullable();
        });

        // NOTE: Only multiplayer matches are registered
        Schema::create('matches', function (Blueprint $table) {
            $table->id();

            // Type of games in the match (Bisca dos 3; Bisca dos 9)
            $table->enum('type', ['3', '9'])->default('3');

            // Player 1 (usually, creates the match)
            $table->unsignedBigInteger('player1_user_id');
            $table->foreign('player1_user_id')->references('id')->on('users');

            // Player 2 (usually, joins the match)
            $table->unsignedBigInteger('player2_user_id');
            $table->foreign('player2_user_id')->references('id')->on('users');

            // Winner (null when not ended)
            $table->unsignedBigInteger('winner_user_id')->nullable();
            $table->foreign('winner_user_id')->references('id')->on('users');

            // Loser (null when not ended)
            $table->unsignedBigInteger('loser_user_id')->nullable();
            $table->foreign('loser_user_id')->references('id')->on('users');

            // Match status
            // Pending - Match is waiting for players
            // Playing - Match is in progress
            // Ended - Match is over
            // Interrupted - Match was interrupted due to technical issues (not finished; no winner)
            // Note: depending of the project's implementation, some status may never be used
            $table->enum('status', ['Pending', 'Playing', 'Ended', 'Interrupted']);

            // The stake for this match
            $table->integer('stake')->default(3);

            // Moment when the match began
            $table->dateTime('began_at')->nullable();
            // Moment when the match ended
            $table->dateTime('ended_at')->nullable();
            // Match total time (in seconds) = ended_at - began_at
            $table->decimal('total_time', 8, 2)->nullable();

            // After the match ended, the total marks for player 1
            $table->integer('player1_marks')->nullable();

            // After the game ended, the total marks for player 2
            $table->integer('player2_marks')->nullable();

            // After the match ended, the sum of points for player 1 (all games)
            $table->integer('player1_points')->nullable();

            // After the match ended, the sum of points for player 2 (all games)
            $table->integer('player2_points')->nullable();

            // custom data
            $table->json('custom')->nullable();

        });

        // NOTE: Only multiplayer games are registered
        Schema::create('games', function (Blueprint $table) {
            $table->id();

            // Type of game (Bisca dos 3; Bisca dos 9)
            $table->enum('type', ['3', '9'])->default('3');

            // Player 1 (usually, creates the game)
            $table->unsignedBigInteger('player1_user_id');
            $table->foreign('player1_user_id')->references('id')->on('users');

            // Player 2 (usually, joins the game)
            $table->unsignedBigInteger('player2_user_id');
            $table->foreign('player2_user_id')->references('id')->on('users');

            // True when the game has ended with a draw
            $table->boolean('is_draw')->default(false);

            // Winner (null when not ended, or when a draw)
            $table->unsignedBigInteger('winner_user_id')->nullable();
            $table->foreign('winner_user_id')->references('id')->on('users');

            // Loser (null when not ended, or when a draw)
            $table->unsignedBigInteger('loser_user_id')->nullable();
            $table->foreign('loser_user_id')->references('id')->on('users');

            // Match (null for standalone games)
            $table->unsignedBigInteger('match_id')->nullable();
            $table->foreign('match_id')->references('id')->on('matches');

            // Game status: 'Pending', 'Playing', 'Ended', or 'Interrupted'.
            // Pending: waiting for players
            // Playing: in progress
            // Ended: finished
            // Interrupted: stopped due to technical issues (unfinished; no winner)
            // Note: depending on the project’s implementation, some statuses may never be used.
            $table->enum('status', ['Pending', 'Playing', 'Ended', 'Interrupted']);

            // Moment when the game began
            $table->dateTime('began_at')->nullable();
            // Moment when the game ended
            $table->dateTime('ended_at')->nullable();
            // Game total time (in seconds) = ended_at - began_at
            $table->decimal('total_time', 8, 2)->nullable();


            // After the game ended, the total of points for player 1
            $table->integer('player1_points')->nullable();

            // After the game ended, the total of points for player 2
            $table->integer('player2_points')->nullable();

            // custom data
            $table->json('custom')->nullable();
        });

        Schema::create('coin_transaction_types', function (Blueprint $table) {
            $table->id();
            $table->string('name', 40);
            // Credit - increase coins
            // Debit - decrease coins
            $table->enum('type', ['C', 'D']);
            // Transaction Types can be deleted with "soft deletes"
            $table->softDeletes();

            // custom data
            $table->json('custom')->nullable();
        });

        // Add default coin_transaction_types
        DB::table('coin_transaction_types')->insert([
            ['name' => 'Bonus', 'type' => 'C'],         // id = 1
            ['name' => 'Coin purchase', 'type' => 'C'], // id = 2
            ['name' => 'Game fee', 'type' => 'D'],      // id = 3
            ['name' => 'Match stake', 'type' => 'D'],   // id = 4
            ['name' => 'Game payout', 'type' => 'C'],      // id = 5
            ['name' => 'Match payout', 'type' => 'C'],     // id = 6
        ]);

        Schema::create('coin_transactions', function (Blueprint $table) {
            $table->id();

            // Datetime of the coin transaction
            $table->dateTime('transaction_datetime');

            // User associated with the transaction
            $table->unsignedBigInteger('user_id');
            $table->foreign('user_id')->references('id')->on('users');

            // Match associated with the transaction (optional - it can be null)
            $table->unsignedBigInteger('match_id')->nullable();
            $table->foreign('match_id')->references('id')->on('matches');

            // Game associated with the transaction (optional - it can be null)
            $table->unsignedBigInteger('game_id')->nullable();
            $table->foreign('game_id')->references('id')->on('games');

            // Type of the transaction
            $table->unsignedBigInteger('coin_transaction_type_id');
            $table->foreign('coin_transaction_type_id')->references('id')->on('coin_transaction_types');

            // Amount of the transaction (coins)
            // Positive -> increments the total amount of brain coins (Credit)
            // Negative -> decrements the total amount of brain coins (Debit)
            $table->integer('coins');

            // custom data
            $table->json('custom')->nullable();
        });

        Schema::create('coin_purchases', function (Blueprint $table) {
            $table->id();

            // Datetime of the coin purchase
            $table->dateTime('purchase_datetime');

            // User associated with the purchase
            $table->unsignedBigInteger('user_id');
            $table->foreign('user_id')->references('id')->on('users');

            // Coin Transaction associated with this purchase
            // One to One relation (purchase must have a coin transaction)
            $table->unsignedBigInteger('coin_transaction_id')->unique();
            $table->foreign('coin_transaction_id')->references('id')->on('coin_transactions');

            // Amount of the purchase (real money in euros)
            $table->decimal('euros', 8, 2);

            // Purchases will involve a payment with a type and a reference
            // MBWAY -  Phone number with 9 digits
            // PAYPAL - eMail
            // IBAN - bank transfer (2 letters + 23 digits)
            // MB - Multibanco payment - entity number (5 digits) + Reference (9 digits))
            // VISA - Visa card number (16 digits)
            $table->enum('payment_type', ['MBWAY', 'PAYPAL', 'IBAN', 'MB', 'VISA']);
            $table->string('payment_reference', 30);

            // custom data
            $table->json('custom')->nullable();
        });
    }
    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::drop('coin_purchases');
        Schema::drop('coin_transactions');
        Schema::drop('coin_transaction_types');
        Schema::drop('games');
        Schema::drop('matches');

        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn('deleted_at');
            $table->dropColumn('custom');
            $table->dropColumn('coins_balance');
            $table->dropColumn('photo_avatar_filename');
            $table->dropColumn('blocked');
            $table->dropColumn('nickname');
            $table->dropColumn('type');
        });
    }
};
