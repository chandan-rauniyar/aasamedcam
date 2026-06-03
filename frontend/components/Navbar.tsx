"use client";

import Link from "next/link";
import { useAuth } from "@/context/AuthContext";
import { LogOut, Package, ShoppingCart, Activity } from "lucide-react";
import { usePathname } from "next/navigation";

export default function Navbar() {
  const { user, logout } = useAuth();
  const pathname = usePathname();

  if (!user || pathname === "/login" || pathname === "/register" || pathname === "/") return null;

  return (
    <nav className="bg-white shadow-sm border-b border-gray-200">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 justify-between">
          <div className="flex">
            <div className="flex flex-shrink-0 items-center">
              <span className="text-xl font-bold text-blue-600 flex items-center gap-2">
                <Activity className="h-6 w-6" />
                AasaMedChem
              </span>
            </div>
            <div className="hidden sm:-my-px sm:ml-6 sm:flex sm:space-x-8">
              {user.role === 'ADMIN' ? (
                <>
                  <Link href="/admin/orders" className="inline-flex items-center border-b-2 border-transparent px-1 pt-1 text-sm font-medium text-gray-500 hover:border-gray-300 hover:text-gray-700">
                    Manage Orders
                  </Link>
                  <Link href="/products" className="inline-flex items-center border-b-2 border-transparent px-1 pt-1 text-sm font-medium text-gray-500 hover:border-gray-300 hover:text-gray-700">
                    Products
                  </Link>
                </>
              ) : (
                <>
                  <Link href="/products" className="inline-flex items-center border-b-2 border-transparent px-1 pt-1 text-sm font-medium text-gray-500 hover:border-gray-300 hover:text-gray-700">
                    <Package className="h-4 w-4 mr-2" />
                    Products
                  </Link>
                  <Link href="/orders" className="inline-flex items-center border-b-2 border-transparent px-1 pt-1 text-sm font-medium text-gray-500 hover:border-gray-300 hover:text-gray-700">
                    <ShoppingCart className="h-4 w-4 mr-2" />
                    My Orders
                  </Link>
                </>
              )}
            </div>
          </div>
          <div className="flex items-center">
            <div className="flex items-center gap-4">
              <span className="text-sm text-gray-700 hidden sm:block">
                Welcome, {user.name} <span className="bg-blue-100 text-blue-800 text-xs px-2 py-0.5 rounded ml-1">{user.role}</span>
              </span>
              <button
                onClick={logout}
                className="flex items-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
              >
                <LogOut className="h-4 w-4 mr-2" />
                Sign out
              </button>
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
}
