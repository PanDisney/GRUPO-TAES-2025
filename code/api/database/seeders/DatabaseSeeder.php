<?php

namespace Database\Seeders;

use App\Models\User;
use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;
use Carbon\Carbon;

class DatabaseSeeder extends Seeder
{
    use WithoutModelEvents;

    public static $startDate;
    public static $dbInsertBlockSize = 500;

    // public static $seedType = "small";
    //public static $seedType = "full";
    //public static $seedLanguage = "pt_PT";
    public static $seedLanguage = "en_US";

    public function run(): void
    {
        $this->command->info("-----------------------------------------------");
        $this->command->info("START of database seeder");
        $this->command->info("-----------------------------------------------");

        self::$startDate = Carbon::now()->subMonths(14);
        self::$seedLanguage = $this->command->choice('What is the language for users\' names?', ['pt_PT', 'en_US'], 0);

        if (DB::getDriverName() === 'sqlite') {
            DB::statement('PRAGMA foreign_keys = OFF');
        } else {
            DB::statement('SET foreign_key_checks=0');
            // No permissions to change global setting. Change the session setting only
            //DB::statement("SET @@global.time_zone = '+00:00'");
            DB::statement("SET time_zone = '+00:00'");
        }

        DB::table('users')->delete();
        DB::table('matches')->delete();
        DB::table('games')->delete();
        DB::table('coin_purchases')->delete();
        DB::table('coin_transactions')->delete();
        DB::table('coin_transaction_types')->delete();

        if (DB::getDriverName() === 'sqlite') {
            DB::statement("DELETE FROM sqlite_sequence WHERE name = 'users'");
            DB::statement("DELETE FROM sqlite_sequence WHERE name = 'matches'");
            DB::statement("DELETE FROM sqlite_sequence WHERE name = 'games'");
            DB::statement("DELETE FROM sqlite_sequence WHERE name = 'coin_purchases'");
            DB::statement("DELETE FROM sqlite_sequence WHERE name = 'coin_transactions'");
            DB::statement("DELETE FROM sqlite_sequence WHERE name = 'coin_transaction_types'");
        } else {
            DB::statement('ALTER TABLE users AUTO_INCREMENT = 0');
            DB::statement('ALTER TABLE matches AUTO_INCREMENT = 0');
            DB::statement('ALTER TABLE games AUTO_INCREMENT = 0');
            DB::statement('ALTER TABLE coin_purchases AUTO_INCREMENT = 0');
            DB::statement('ALTER TABLE coin_transactions AUTO_INCREMENT = 0');
            DB::statement('ALTER TABLE coin_transaction_types AUTO_INCREMENT = 0');
        }

        $this->command->info("-----------------------------------------------");

        $this->call(TransactionTypesSeeder::class);
        $this->call(UsersSeeder::class);
        $this->call(InitialTransactionsSeeder::class);
        $this->call(GamesSeeder::class);
        $this->call(GamesTransactionsSeeder::class);

        if (DB::getDriverName() === 'sqlite') {
            DB::statement('PRAGMA foreign_keys = ON');
        } else {
            DB::statement('SET foreign_key_checks=1');
        }



        $this->command->info("-----------------------------------------------");
        $this->command->info("END of database seeder");
        $this->command->info("-----------------------------------------------");
    }
}
