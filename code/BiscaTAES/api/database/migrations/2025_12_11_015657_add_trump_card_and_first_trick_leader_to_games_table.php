<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::table('games', function (Blueprint $table) {
            $table->string('trump_card', 20)->nullable()->after('custom'); // e.g., ACE_OF_HEARTS
            $table->unsignedBigInteger('first_trick_leader_id')->nullable()->after('trump_card');
            $table->foreign('first_trick_leader_id')->references('id')->on('users');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('games', function (Blueprint $table) {
            $table->dropForeign(['first_trick_leader_id']);
            $table->dropColumn('first_trick_leader_id');
            $table->dropColumn('trump_card');
        });
    }
};
