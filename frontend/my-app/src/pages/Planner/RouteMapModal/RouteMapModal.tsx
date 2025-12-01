import React, { useState, useEffect, useRef } from 'react';
import { RouteAssignment } from '../../../types';
import { plannerService, RouteResponse, ParcelResponse } from '../../../services/plannerService';
import RouteStopsList from './RouteStopsList';
import RouteInfo from './RouteInfo';
import './RouteMapModal.css';

interface RouteMapModalProps {
  assignment: RouteAssignment | null;
  onReturn: () => void;
}

export default function RouteMapModal({ assignment, onReturn }: RouteMapModalProps) {
  const [routeDetails, setRouteDetails] = useState<RouteResponse | null>(null);
  const [parcelStatuses, setParcelStatuses] = useState<Map<number, string>>(new Map());
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const has429ErrorRef = useRef(false);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    if (assignment?.routeId) {
      loadRouteDetails();
    }
  }, [assignment?.routeId]);

  useEffect(() => {
    if (routeDetails && routeDetails.routeStops) {
      // 从 routeDetails 中提取已有的包裹状态
      // routeDetails 中已经包含最新的包裹状态，无需自动刷新
      const initialStatusMap = new Map<number, string>();
      routeDetails.routeStops.forEach(stop => {
        if (stop.parcelsToDeliver) {
          stop.parcelsToDeliver.forEach(parcel => {
            initialStatusMap.set(parcel.parcelId, parcel.status);
          });
        }
      });
      setParcelStatuses(initialStatusMap);
      
      // 重置 429 错误标志
      has429ErrorRef.current = false;
      
      // 清除之前的 interval（如果存在）
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
      
      // ✅ 最优方案：完全禁用自动刷新
      // 原因：
      // 1. routeDetails 中已经包含最新的包裹状态
      // 2. 避免触发 429 错误（后端限流：60 请求/分钟）
      // 3. 减少不必要的 API 请求
      // 4. UI 可以正常显示，使用已有数据
      // 
      // 如果需要刷新状态，可以：
      // - 添加手动刷新按钮
      // - 在用户返回页面时刷新
      // - 使用页面可见性 API 在页面重新聚焦时刷新
      
      return () => {
        if (intervalRef.current) {
          clearInterval(intervalRef.current);
          intervalRef.current = null;
        }
      };
    }
  }, [routeDetails]);

  const loadRouteDetails = async () => {
    if (!assignment?.routeId) return;

    setLoading(true);
    setError('');
    try {
      const route = await plannerService.getRouteById(assignment.routeId);
      setRouteDetails(route);
    } catch (err: any) {
      console.error('Error loading route details:', err);
      setError(err.message || 'Failed to load route details.');
    } finally {
      setLoading(false);
    }
  };

  const refreshParcelStatuses = async () => {
    if (!routeDetails || !routeDetails.routeStops) return;
    
    // 如果之前遇到 429 错误，跳过本次刷新
    if (has429ErrorRef.current) {
      console.warn('Skipping parcel status refresh due to previous 429 error');
      return;
    }

    const parcelIds: number[] = [];
    routeDetails.routeStops.forEach(stop => {
      if (stop.parcelsToDeliver) {
        stop.parcelsToDeliver.forEach(parcel => {
          parcelIds.push(parcel.parcelId);
        });
      }
    });

    if (parcelIds.length === 0) return;

    const statusMap = new Map<number, string>();
    let has429 = false;
    
    // 使用串行请求或批量请求，避免同时发送太多请求
    // 每批处理 5 个包裹，批次之间延迟 500ms
    const batchSize = 5;
    for (let i = 0; i < parcelIds.length; i += batchSize) {
      const batch = parcelIds.slice(i, i + batchSize);
      
      await Promise.all(
        batch.map(async (parcelId) => {
          try {
            const parcel = await plannerService.getParcelById(parcelId);
            statusMap.set(parcelId, parcel.status);
          } catch (err: any) {
            console.error(`Error fetching parcel ${parcelId}:`, err);
            // 如果遇到 429 错误，设置标志并停止后续请求
            // 检查错误消息中是否包含 429 状态码或相关错误信息
            const errorMessage = err.message || '';
            if (errorMessage.includes('429') || 
                errorMessage.includes('Too many requests') ||
                errorMessage.includes('Please try again later')) {
              has429 = true;
              has429ErrorRef.current = true;
              // 清除 interval，停止自动刷新
              if (intervalRef.current) {
                clearInterval(intervalRef.current);
                intervalRef.current = null;
              }
            }
          }
        })
      );
      
      // 如果遇到 429，停止处理剩余批次
      if (has429) {
        break;
      }
      
      // 批次之间延迟，避免触发限流
      if (i + batchSize < parcelIds.length) {
        await new Promise(resolve => setTimeout(resolve, 500));
      }
    }
    
    // 只更新成功获取的状态
    if (statusMap.size > 0) {
      setParcelStatuses(prev => {
        const updated = new Map(prev);
        statusMap.forEach((status, id) => {
          updated.set(id, status);
        });
        return updated;
      });
    }
    
    // 如果遇到 429，显示警告但不阻止 UI 显示
    if (has429) {
      console.warn('Rate limit reached. Parcel status refresh stopped. Using cached data.');
    }
  };

  if (!assignment) {
    return (
      <div className="route-map-page">
        <div className="route-map-container-page">
          <div style={{ padding: '40px', textAlign: 'center' }}>No assignment selected</div>
          <div className="return-button-container">
            <button className="return-button" onClick={onReturn}>
              Return
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="route-map-page">
      <div className="route-map-container-page">
        <div className="route-map-header-page">
          <div className="route-map-truck-plate">{assignment.truckPlateNo}</div>
        </div>
        <div className="route-map-content">
          {loading && (
            <div style={{ padding: '40px', textAlign: 'center' }}>Loading route...</div>
          )}
          {error && (
            <div style={{ padding: '40px', color: 'red', textAlign: 'center' }}>{error}</div>
          )}
          {!loading && !error && routeDetails && (
            <RouteStopsList 
              routeStops={routeDetails.routeStops} 
              parcelStatuses={parcelStatuses}
              routeStartTime={routeDetails.startTime}
              routeTotalTransportTime={routeDetails.totalTransportTime}
            />
          )}
          {!loading && !error && !routeDetails && (
            <div style={{ padding: '40px', textAlign: 'center' }}>No route details available</div>
          )}
        </div>
        {routeDetails && <RouteInfo routeDetails={routeDetails} />}
        <div className="return-button-container">
          <button className="return-button" onClick={onReturn}>
            Return
          </button>
        </div>
      </div>
    </div>
  );
}

