"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { ShoppingCart } from "lucide-react";

interface OrderItem {
  id: number;
  productId: number;
  orderedUnit: string;
  orderedQty: number;
  baseQty: number;
  unitPriceInr: number;
  lineTotalInr: number;
}

interface Order {
  id: number;
  status: string;
  totalInr: number;
  notes: string;
  items: OrderItem[];
  createdAt: string;
}

export default function MyOrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const res = await api.get("/api/orders/mine");
        setOrders(res.data.content || res.data);
      } catch (err) {
        console.error("Failed to fetch orders", err);
      } finally {
        setLoading(false);
      }
    };
    fetchOrders();
  }, []);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING': return 'bg-yellow-50 text-yellow-800 ring-yellow-600/20';
      case 'CONFIRMED': return 'bg-blue-50 text-blue-800 ring-blue-600/20';
      case 'DISPATCHED': return 'bg-purple-50 text-purple-800 ring-purple-600/20';
      case 'CANCELLED': return 'bg-red-50 text-red-800 ring-red-600/20';
      default: return 'bg-gray-50 text-gray-800 ring-gray-600/20';
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">My Orders</h1>
        <p className="text-gray-500 mt-1">View your order history and status</p>
      </div>

      {loading ? (
        <div className="space-y-4">
          {[1,2,3].map(i => <div key={i} className="h-24 bg-white rounded-xl shadow-sm border border-gray-100 animate-pulse"></div>)}
        </div>
      ) : orders.length === 0 ? (
        <div className="text-center py-20 bg-white rounded-xl shadow-sm border border-gray-100">
          <ShoppingCart className="mx-auto h-12 w-12 text-gray-300" />
          <h3 className="mt-2 text-sm font-semibold text-gray-900">No orders yet</h3>
          <p className="mt-1 text-sm text-gray-500">When you place orders, they will appear here.</p>
        </div>
      ) : (
        <div className="space-y-6">
          {orders.map((order) => (
            <div key={order.id} className="bg-white shadow-sm border border-gray-200 rounded-xl overflow-hidden">
              <div className="border-b border-gray-200 bg-gray-50 px-6 py-4 flex items-center justify-between">
                <div>
                  <h3 className="text-sm font-semibold text-gray-900">Order #{order.id}</h3>
                  <p className="text-xs text-gray-500 mt-1">{new Date(order.createdAt).toLocaleString()}</p>
                </div>
                <div className="flex items-center gap-4">
                  <span className={`inline-flex items-center rounded-md px-2 py-1 text-xs font-medium ring-1 ring-inset ${getStatusColor(order.status)}`}>
                    {order.status}
                  </span>
                  <span className="text-lg font-bold text-gray-900">₹{order.totalInr}</span>
                </div>
              </div>
              <div className="px-6 py-4">
                <ul className="divide-y divide-gray-100">
                  {order.items.map((item) => (
                    <li key={item.id} className="flex justify-between py-3 text-sm">
                      <div>
                        <span className="font-medium text-gray-900">Product ID: {item.productId}</span>
                        <span className="text-gray-500 ml-2">({item.orderedQty} {item.orderedUnit})</span>
                      </div>
                      <div className="text-gray-900 font-medium">₹{item.lineTotalInr}</div>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
