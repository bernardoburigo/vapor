const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

/** Preço em reais; zero vira "Gratuito". */
export function formatPrice(valor: number): string {
  return valor === 0 ? 'Gratuito' : moeda.format(valor)
}

/** Nota com uma casa decimal e vírgula; sem avaliações mostra um travessão. */
export function formatRating(nota: number | null | undefined): string {
  return nota == null ? '—' : nota.toFixed(1).replace('.', ',')
}

/**
 * Aceita "2026-11-15" ou "2026-11-02T21:35:45.5": usa só a parte da data, sem passar por Date,
 * para o fuso horário não deslocar o dia.
 */
export function formatDate(valor: string | null | undefined): string {
  if (!valor) return '—'
  const casou = /^(\d{4})-(\d{2})-(\d{2})/.exec(valor)
  return casou ? `${casou[3]}/${casou[2]}/${casou[1]}` : valor
}

export function pluralize(quantidade: number, singular: string, plural: string): string {
  return `${quantidade} ${quantidade === 1 ? singular : plural}`
}
