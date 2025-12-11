<?php

namespace Database\Seeders;

use App\Models\CardFace;
use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;

class CardFacesSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        CardFace::create([
            'name' => 'Classic',
            'cost' => 0,
            'image_name' => 'back_card_red'
        ]);

        CardFace::create([
            'name' => 'Green',
            'cost' => 100,
            'image_name' => 'back_card_green'
        ]);

        CardFace::create([
            'name' => 'Yellow',
            'cost' => 150,
            'image_name' => 'back_card_yellow'
        ]);

        CardFace::create([
            'name' => 'Abandoned',
            'cost' => 200,
            'image_name' => 'back_card_abandoned'
        ]);

        CardFace::create([
            'name' => 'Silver',
            'cost' => 200,
            'image_name' => 'back_card_silver'
        ]);

        CardFace::create([
            'name' => 'Black',
            'cost' => 50,
            'image_name' => 'back_card_black'
        ]);

        CardFace::create([
            'name' => 'Rainbow',
            'cost' => 500,
            'image_name' => 'back_card_rainbow'
        ]);

        CardFace::create([
            'name' => 'Purple',
            'cost' => 25,
            'image_name' => 'back_card_purple'
        ]);
    }
}
