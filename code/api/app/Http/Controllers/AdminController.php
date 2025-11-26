<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Transaction;
use App\Models\User; // Import the User model
use App\Http\Resources\UserResource; // Import the UserResource

class AdminController extends Controller
{
    public function getAllTransactions(Request $request)
    {
        $transactions = Transaction::orderBy('created_at', 'desc')->get();
        return response()->json($transactions);
    }

    public function index()
    {
        $users = User::where('type', '!=', 'A')->orderBy('name')->get(); // Fetch non-admin users
        return UserResource::collection($users); // Return them using UserResource
    }
}

