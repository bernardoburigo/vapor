import { describe, expect, it } from 'vitest'
import { formatDate, formatPrice, formatRating, pluralize } from './format'

/** O Intl separa "R$" do valor com espaço sem quebra; normaliza para comparar. */
const semNbsp = (texto: string) => texto.replace(/\u00a0/g, ' ')

describe('formatPrice', () => {
  it('formata em reais', () => {
    expect(semNbsp(formatPrice(199.9))).toBe('R$ 199,90')
    expect(semNbsp(formatPrice(1234.5))).toBe('R$ 1.234,50')
  })

  it('mostra "Gratuito" para zero', () => {
    expect(formatPrice(0)).toBe('Gratuito')
  })
})

describe('formatRating', () => {
  it('usa vírgula e uma casa decimal', () => {
    expect(formatRating(4.5)).toBe('4,5')
    expect(formatRating(5)).toBe('5,0')
  })

  it('mostra travessão sem avaliações', () => {
    expect(formatRating(null)).toBe('—')
    expect(formatRating(undefined)).toBe('—')
  })
})

describe('formatDate', () => {
  it('converte data ISO sem deslocar o dia por fuso horário', () => {
    expect(formatDate('2026-11-15')).toBe('15/11/2026')
    expect(formatDate('2026-01-01')).toBe('01/01/2026')
  })

  it('aceita data e hora, ignorando a hora', () => {
    expect(formatDate('2026-11-02T21:35:45.502394')).toBe('02/11/2026')
  })

  it('devolve travessão para vazio', () => {
    expect(formatDate(null)).toBe('—')
    expect(formatDate('')).toBe('—')
  })
})

describe('pluralize', () => {
  it('escolhe singular ou plural', () => {
    expect(pluralize(1, 'avaliação', 'avaliações')).toBe('1 avaliação')
    expect(pluralize(0, 'avaliação', 'avaliações')).toBe('0 avaliações')
    expect(pluralize(12, 'avaliação', 'avaliações')).toBe('12 avaliações')
  })
})
