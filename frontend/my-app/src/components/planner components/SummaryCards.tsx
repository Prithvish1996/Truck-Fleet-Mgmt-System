import React from 'react';

interface SummaryCard {
  title: string;
  value: string;
  delta: string;
  trend: 'up' | 'down' | 'warning';
}

interface SummaryCardsProps {
  cards: SummaryCard[];
}

export default function SummaryCards({ cards }: SummaryCardsProps) {
  return (
    <section className="summary-cards">
      {cards.map(card => (
        <div key={card.title} className="summary-card">
          <div className="summary-card-top">
            <span className="summary-card-title">{card.title}</span>
            <span className={`summary-card-delta summary-card-delta-${card.trend}`}>
              {card.delta}
            </span>
          </div>
          <span className="summary-card-value">{card.value}</span>
        </div>
      ))}
    </section>
  );
}

