<?php

namespace Database\Seeders;

use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;
use stdClass;

class InitialTransactionsSeeder extends Seeder
{
    private $allPlayers;
    private $sizeOfSaveBlock = 200;
    private $valueOfInitialBonus = 10;
    public function run(): void
    {
        $this->allPlayers = DB::table('users')->where('type', 'P')->get();
        $this->command->info("Create initial Bonuses transactions");

        $i = $this->sizeOfSaveBlock;
        $dataBlock = [];
        foreach ($this->allPlayers as $player) {
            if ($i <= 0) {
                $this->saveDataBlock($dataBlock);
                $i = $this->sizeOfSaveBlock;
                $dataBlock = [];
            }
            $dataBlock[] = $this->createBonusTransaction($player);
            $i--;
        }
        if (!empty($dataBlock)) {
            $this->saveDataBlock($dataBlock);
        }
        $this->updateAllPlayerBalances();
    }

    private function saveDataBlock($dataBlock): void
    {
        $size = count($dataBlock);
        DB::table('coin_transactions')->insert($dataBlock);
        $this->command->info("Saved $size initial bonuses transactions");
    }

    private function createBonusTransaction($player): array
    {
        return [
            'transaction_datetime' => $player->created_at,
            'user_id' => $player->id,
            'match_id' => null,
            'game_id' => null,
            'coin_transaction_type_id' => 1,
            'coins' => $this->valueOfInitialBonus,
            'custom' => null,
        ];
    }

    private function updateAllPlayerBalances(): void
    {
        DB::table('users')->where('type', 'P')->update(['coins_balance' => $this->valueOfInitialBonus]);
        $this->command->info("Updated all players initial Balance");
    }
}
