/**
 * O backend não guarda imagem de jogo, então a capa é gerada a partir do título:
 * duas cores e as iniciais, sempre iguais para o mesmo título.
 */
export interface CoverSpec {
  hueA: number
  hueB: number
  angle: number
  initials: string
}

/** Hash FNV-1a de 32 bits: rápido e estável entre execuções. */
export function hashString(texto: string): number {
  let hash = 0x811c9dc5
  for (let i = 0; i < texto.length; i++) {
    hash ^= texto.charCodeAt(i)
    hash = Math.imul(hash, 0x01000193)
  }
  return hash >>> 0
}

export function initialsOf(titulo: string): string {
  const palavras = titulo.split(/[^\p{L}\p{N}]+/u).filter(Boolean)
  if (palavras.length === 0) return '?'
  if (palavras.length === 1) return Array.from(palavras[0]).slice(0, 2).join('').toUpperCase()
  return (Array.from(palavras[0])[0] + Array.from(palavras[1])[0]).toUpperCase()
}

export function coverFor(titulo: string): CoverSpec {
  const hash = hashString(titulo.trim().toLowerCase())
  const hueA = hash % 360
  const hueB = (hueA + 40 + ((hash >>> 9) % 80)) % 360
  const angle = 120 + ((hash >>> 17) % 120)
  return { hueA, hueB, angle, initials: initialsOf(titulo) }
}

/** Gradiente CSS escuro o bastante para as iniciais brancas terem bom contraste. */
export function coverGradient({ hueA, hueB, angle }: CoverSpec): string {
  return `linear-gradient(${angle}deg, hsl(${hueA} 70% 40%), hsl(${hueB} 68% 18%))`
}
