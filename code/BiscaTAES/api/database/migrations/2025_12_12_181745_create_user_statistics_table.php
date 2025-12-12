<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    public function up(): void {
        Schema::create('user_statistics', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained()->onDelete('cascade');
            $table->string('email')->unique();
            $table->integer('total_wins')->default(0);
            $table->integer('total_matches')->default(0);
            $table->integer('coins_earned')->default(0);
            $table->integer('current_coins')->default(0);
            $table->float('win_rate')->default(0.0);
            $table->timestamps();
        });
    }

    public function down(): void {
        Schema::dropIfExists('user_statistics');
    }
};


