<?php

namespace Database\Seeders;

use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;

class TransactionTypesSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $this->command->line("Creating Coin Transaction Types.");

        // Add default coin_transaction_types
        DB::table('coin_transaction_types')->insert([
            ['id' => 1, 'name' => 'Bonus', 'type' => 'C'],         // id = 1
            ['id' => 2, 'name' => 'Coin purchase', 'type' => 'C'], // id = 2
            ['id' => 3, 'name' => 'Game fee', 'type' => 'D'],      // id = 3
            ['id' => 4, 'name' => 'Match stake', 'type' => 'D'],   // id = 4
            ['id' => 5, 'name' => 'Game payout', 'type' => 'C'],      // id = 5
            ['id' => 6, 'name' => 'Match payout', 'type' => 'C'],     // id = 6
        ]);

        $this->command->line("Coin Transaction Types Created Successfully.");
    }
}
