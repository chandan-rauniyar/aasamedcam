"use client";

import { useEffect, useState, use } from "react";
import { api } from "@/lib/api";
import { useRouter } from "next/navigation";
import { ArrowLeft, CheckCircle2 } from "lucide-react";
import Link from "next/link";

interface Product {
  id: number;
  name: string;
  sku: string;
  description: string;
  category: string;
  baseUnit: string;
  basePrice: number;
  stockInBase: number;
}

export default function ProductDetailsPage({ params }: { params: Promise<{ id: string }> }) {
  const resolvedParams = use(params);
  const { id } = resolvedParams;
  const router = useRouter();
  
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  
  // Order form state
  const [qty, setQty] = useState<number | "">("");
  const [unit, setUnit] = useState<string>("");
  const [previewTotal, setPreviewTotal] = useState<number | null>(null);
  const [ordering, setOrdering] = useState(false);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const res = await api.get(`/api/products/${id}`);
        setProduct(res.data);
        setUnit(res.data.baseUnit); // default to base unit
      } catch (err: any) {
        setError(err.response?.data?.message || "Failed to load product");
      } finally {
        setLoading(false);
      }
    };
    fetchProduct();
  }, [id]);

  // Live price preview
  useEffect(() => {
    if (!qty || !unit) {
      setPreviewTotal(null);
      return;
    }
    
    const fetchPreview = async () => {
      try {
        const res = await api.get(`/api/products/${id}/price-preview`, {
          params: { qty, unit }
        });
        // API returns something like { lineTotalInr: 1500.00 }
        setPreviewTotal(res.data.lineTotalInr ?? res.data);
      } catch (err) {
        setPreviewTotal(null);
      }
    };
    
    const timeout = setTimeout(fetchPreview, 300);
    return () => clearTimeout(timeout);
  }, [id, qty, unit]);

  const handleOrder = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!qty || !unit) return;
    
    setOrdering(true);
    setError("");
    
    try {
      await api.post("/api/orders", {
        items: [{
          productId: id,
          orderedQty: Number(qty),
          orderedUnit: unit
        }],
        notes: "Placed via Web UI"
      });
      setSuccess(true);
      setTimeout(() => {
        router.push("/orders");
      }, 2000);
    } catch (err: any) {
      setError(err.response?.data?.message || "Failed to place order.");
      setOrdering(false);
    }
  };

  const getAllowedUnits = (base: string) => {
    if (base === "g") return ["g", "kg"];
    if (base === "mL") return ["mL", "L"];
    if (base === "count") return ["count"];
    return [base];
  };

  if (loading) return <div className="p-8 max-w-3xl mx-auto animate-pulse h-64 bg-white rounded-xl"></div>;
  if (error || !product) return <div className="p-8 max-w-3xl mx-auto text-red-500">{error}</div>;

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <Link href="/products" className="inline-flex items-center text-sm font-medium text-blue-600 hover:text-blue-500 mb-6">
        <ArrowLeft className="mr-2 h-4 w-4" />
        Back to Products
      </Link>
      
      <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">
        <div className="p-8">
          <div className="flex justify-between items-start mb-2">
            <h1 className="text-3xl font-bold text-gray-900">{product.name}</h1>
            <span className="inline-flex items-center rounded-full bg-blue-50 px-3 py-1 text-sm font-medium text-blue-700">
              {product.category}
            </span>
          </div>
          <p className="text-sm text-gray-500 mb-6">SKU: {product.sku}</p>
          
          <div className="prose prose-blue max-w-none mb-8">
            <p className="text-gray-700">{product.description}</p>
          </div>
          
          <div className="grid grid-cols-2 gap-4 py-6 border-y border-gray-100 mb-8 bg-gray-50/50 rounded-lg px-6">
            <div>
              <p className="text-sm text-gray-500 font-medium">Base Price</p>
              <p className="text-xl font-semibold text-gray-900">₹{product.basePrice} <span className="text-sm font-normal text-gray-500">/ {product.baseUnit}</span></p>
            </div>
            <div>
              <p className="text-sm text-gray-500 font-medium">Available Stock</p>
              <p className="text-xl font-semibold text-gray-900">{product.stockInBase} <span className="text-sm font-normal text-gray-500">{product.baseUnit}</span></p>
            </div>
          </div>

          {success ? (
            <div className="rounded-xl bg-green-50 p-6 flex flex-col items-center text-center">
              <CheckCircle2 className="h-12 w-12 text-green-500 mb-3" />
              <h3 className="text-lg font-medium text-green-800">Order Placed Successfully!</h3>
              <p className="text-sm text-green-700 mt-2">Redirecting to your orders...</p>
            </div>
          ) : (
            <form onSubmit={handleOrder} className="bg-white border border-gray-200 rounded-xl p-6 shadow-sm">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Place an Order</h3>
              
              {error && <div className="mb-4 text-sm text-red-600 bg-red-50 p-3 rounded-md">{error}</div>}
              
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 mb-6">
                <div>
                  <label htmlFor="qty" className="block text-sm font-medium leading-6 text-gray-900">
                    Quantity
                  </label>
                  <input
                    type="number"
                    step="any"
                    id="qty"
                    required
                    min="0.0001"
                    className="mt-2 block w-full rounded-md border-0 py-2 px-3 text-gray-900 ring-1 ring-inset ring-gray-300 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm sm:leading-6"
                    value={qty}
                    onChange={(e) => setQty(e.target.value === "" ? "" : Number(e.target.value))}
                  />
                </div>
                <div>
                  <label htmlFor="unit" className="block text-sm font-medium leading-6 text-gray-900">
                    Unit
                  </label>
                  <select
                    id="unit"
                    required
                    className="mt-2 block w-full rounded-md border-0 py-2 pl-3 pr-10 text-gray-900 ring-1 ring-inset ring-gray-300 focus:ring-2 focus:ring-blue-600 sm:text-sm sm:leading-6"
                    value={unit}
                    onChange={(e) => setUnit(e.target.value)}
                  >
                    {getAllowedUnits(product.baseUnit).map(u => (
                      <option key={u} value={u}>{u}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="flex items-center justify-between border-t border-gray-100 pt-6 mt-6">
                <div>
                  <p className="text-sm text-gray-500">Estimated Total</p>
                  <p className="text-2xl font-bold text-gray-900">
                    {previewTotal !== null ? `₹${previewTotal}` : "—"}
                  </p>
                </div>
                <button
                  type="submit"
                  disabled={ordering || !qty || !unit}
                  className="rounded-md bg-blue-600 px-6 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-blue-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600 disabled:opacity-50 transition-colors"
                >
                  {ordering ? "Processing..." : "Place Order"}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
