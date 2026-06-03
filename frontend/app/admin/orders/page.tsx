"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { Activity } from "lucide-react";

interface Order {
  id: number;
  userId: number;
  status: string;
  totalInr: number;
  createdAt: string;
  items: any[];
}

export default function AdminOrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [stats, setStats] = useState<any>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [ordersRes, statsRes] = await Promise.all([
        api.get("/api/orders"),
        api.get("/api/orders/stats")
      ]);
      setOrders(ordersRes.data.content || ordersRes.data);
      setStats(statsRes.data);
    } catch (err) {
      console.error("Failed to fetch admin data", err);
    } finally {
      setLoading(false);
    }
  };

  const updateStatus = async (id: number, newStatus: string) => {
    try {
      await api.patch(`/api/orders/${id}/status`, { status: newStatus });
      fetchData(); // refresh list
    } catch (err) {
      alert("Failed to update status");
    }
  };

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
        <h1 className="text-2xl font-bold text-gray-900">Order Management</h1>
        <p className="text-gray-500 mt-1">Admin dashboard for incoming orders</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-4 mb-8">
        {['PENDING', 'CONFIRMED', 'DISPATCHED', 'CANCELLED'].map(status => (
          <div key={status} className="bg-white p-6 rounded-xl shadow-sm border border-gray-200 flex flex-col items-center justify-center">
            <dt className="text-sm font-medium text-gray-500 truncate">{status}</dt>
            <dd className="mt-1 text-3xl font-semibold tracking-tight text-gray-900">{stats[status] || 0}</dd>
          </div>
        ))}
      </div>

      <div className="bg-white shadow-sm border border-gray-200 rounded-xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Order ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">User ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Total (INR)</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {loading ? (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-sm text-gray-500">Loading...</td>
                </tr>
              ) : orders.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-sm text-gray-500">No orders found.</td>
                </tr>
              ) : (
                orders.map((order) => (
                  <tr key={order.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">#{order.id}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{new Date(order.createdAt).toLocaleDateString()}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{order.userId}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">₹{order.totalInr}</td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex items-center rounded-md px-2 py-1 text-xs font-medium ring-1 ring-inset ${getStatusColor(order.status)}`}>
                        {order.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <select
                        className="rounded border-gray-300 text-sm focus:ring-blue-500 focus:border-blue-500"
                        value={order.status}
                        onChange={(e) => updateStatus(order.id, e.target.value)}
                      >
                        <option value="PENDING">PENDING</option>
                        <option value="CONFIRMED">CONFIRMED</option>
                        <option value="DISPATCHED">DISPATCHED</option>
                        <option value="CANCELLED">CANCELLED</option>
                      </select>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
