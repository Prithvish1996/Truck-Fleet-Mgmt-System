import React, { useState } from 'react';
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  DragEndEvent,
} from '@dnd-kit/core';
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
  useSortable,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { RouteResponse, StopDto } from '../../../services/plannerService';
import { getStopAddress, formatParcelId } from '../../../utils/dataTransformers';
import './RouteStopsEditModal.css';

interface RouteStopsEditModalProps {
  routes: RouteResponse[];
  onClose: () => void;
  onSave: (updatedRoutes: RouteResponse[]) => void;
}

interface SortableStopItemProps {
  stop: StopDto;
  routeId: number;
  index: number;
}

function SortableStopItem({ stop, routeId, index }: SortableStopItemProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: `${routeId}-${stop.stopId}` });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  const firstParcel = stop.parcelsToDeliver?.[0];
  const address = firstParcel ? getStopAddress(stop, firstParcel) : 'No address';

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={`stop-item ${isDragging ? 'dragging' : ''}`}
      {...attributes}
    >
      <div className="stop-item-header">
        <div className="drag-handle" {...listeners}>
          <span className="drag-icon">⋮⋮</span>
        </div>
        <div className="stop-number">Stop {index + 1}</div>
      </div>
      <div className="stop-address">{address}</div>
      <div className="stop-parcels">
        <strong>Parcels:</strong>{' '}
        {stop.parcelsToDeliver?.map((p, idx) => (
          <span key={p.parcelId} className="parcel-badge">
            {formatParcelId(p.parcelId)}
            {idx < (stop.parcelsToDeliver?.length || 0) - 1 ? ', ' : ''}
          </span>
        )) || 'None'}
      </div>
      <div className="stop-type">
        <span className={`type-badge ${stop.stopType?.toLowerCase()}`}>
          {stop.stopType}
        </span>
      </div>
    </div>
  );
}
// maps to be discussed with Duncan
export default function RouteStopsEditModal({
  routes,
  onClose,
  onSave,
}: RouteStopsEditModalProps) {
  const [localRoutes, setLocalRoutes] = useState<RouteResponse[]>(
    routes.map((route) => ({
      ...route,
      routeStops: [...(route.routeStops || [])],
    }))
  );

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;

    if (!over || active.id === over.id) {
      return;
    }

    const activeId = active.id.toString();
    const overId = over.id.toString();

    const [activeRouteId, activeStopId] = activeId.split('-').map(Number);
    const [overRouteId, overStopId] = overId.split('-').map(Number);

    if (activeRouteId !== overRouteId) {
      return;
    }

    setLocalRoutes((prevRoutes) => {
      return prevRoutes.map((route) => {
        if (route.routeId !== activeRouteId) {
          return route;
        }

        const stops = [...(route.routeStops || [])];
        const oldIndex = stops.findIndex((s) => s.stopId === activeStopId);
        const newIndex = stops.findIndex((s) => s.stopId === overStopId);

        if (oldIndex === -1 || newIndex === -1) {
          return route;
        }

        const reorderedStops = arrayMove(stops, oldIndex, newIndex);
        
        const updatedStops = reorderedStops.map((stop, idx) => ({
          ...stop,
          priority: idx + 1,
        }));

        return {
          ...route,
          routeStops: updatedStops,
        };
      });
    });
  };

  const handleSave = () => {
    onSave(localRoutes);
  };

  const getEditableStops = (route: RouteResponse): StopDto[] => {
    return (route.routeStops || []).filter(
      (stop) => stop.parcelsToDeliver && stop.parcelsToDeliver.length > 0
    );
  };

  return (
    <div className="route-stops-edit-modal-overlay" onClick={onClose}>
      <div
        className="route-stops-edit-modal-content"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="modal-header">
          <h2>Edit Route Stops Order</h2>
          <button className="close-button" onClick={onClose}>
            ×
          </button>
        </div>

        <div className="modal-body">
          {localRoutes.length === 0 ? (
            <div className="no-routes">No routes available</div>
          ) : (
            localRoutes.map((route) => {
              const editableStops = getEditableStops(route);
              
              if (editableStops.length === 0) {
                return null;
              }

              return (
                <div key={route.routeId} className="route-section">
                  <h3 className="route-title">
                    Route #{route.routeId}
                    {route.truckPlateNumber && (
                      <span className="truck-plate"> - {route.truckPlateNumber}</span>
                    )}
                  </h3>
                  <p className="route-info">
                    {editableStops.length} stops • Distance: {route.totalDistance?.toFixed(2) || 0} km
                  </p>

                  <DndContext
                    sensors={sensors}
                    collisionDetection={closestCenter}
                    onDragEnd={handleDragEnd}
                  >
                    <SortableContext
                      items={editableStops.map(
                        (stop) => `${route.routeId}-${stop.stopId}`
                      )}
                      strategy={verticalListSortingStrategy}
                    >
                      <div className="stops-list">
                        {editableStops.map((stop, index) => (
                          <SortableStopItem
                            key={`${route.routeId}-${stop.stopId}`}
                            stop={stop}
                            routeId={route.routeId}
                            index={index}
                          />
                        ))}
                      </div>
                    </SortableContext>
                  </DndContext>
                </div>
              );
            })
          )}
        </div>

        <div className="modal-footer">
          <button className="btn-back" onClick={onClose}>
            Back
          </button>
          <button className="btn-save" onClick={handleSave}>
            Save
          </button>
        </div>
      </div>
    </div>
  );
}

