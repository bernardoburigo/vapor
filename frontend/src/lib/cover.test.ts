import { describe, expect, it } from 'vitest'
import { coverFor, coverGradient, hashString, initialsOf } from './cover'

describe('capa gerada', () => {
  it('é a mesma para o mesmo título, sem diferenciar caixa nem espaços nas pontas', () => {
    expect(coverFor('Hades')).toEqual(coverFor('  hades '))
  })

  it('muda de título para título', () => {
    expect(coverFor('Hades')).not.toEqual(coverFor('Celeste'))
  })

  it('mantém matizes, ângulo e gradiente em faixas válidas', () => {
    for (const titulo of ['Hades', 'Celeste', 'The Witcher 3: Wild Hunt', 'Dandara', 'X', '日本語']) {
      const capa = coverFor(titulo)
      expect(capa.hueA).toBeGreaterThanOrEqual(0)
      expect(capa.hueA).toBeLessThan(360)
      expect(capa.hueB).toBeGreaterThanOrEqual(0)
      expect(capa.hueB).toBeLessThan(360)
      expect(capa.angle).toBeGreaterThanOrEqual(120)
      expect(capa.angle).toBeLessThan(240)
      expect(coverGradient(capa)).toMatch(/^linear-gradient\(\d+deg, hsl\(.+\), hsl\(.+\)\)$/)
    }
  })

  it('o hash é determinístico e sem sinal', () => {
    expect(hashString('vapor')).toBe(hashString('vapor'))
    expect(hashString('vapor')).toBeGreaterThanOrEqual(0)
  })
})

describe('iniciais', () => {
  it('usa a primeira letra das duas primeiras palavras', () => {
    expect(initialsOf('Half-Life 3')).toBe('HL')
    expect(initialsOf('The Witcher 3: Wild Hunt')).toBe('TW')
  })

  it('usa as duas primeiras letras quando há uma palavra só', () => {
    expect(initialsOf('Hades')).toBe('HA')
    expect(initialsOf('Q')).toBe('Q')
  })

  it('lida com acentos e títulos vazios', () => {
    expect(initialsOf('Ação Total')).toBe('AT')
    expect(initialsOf('   ')).toBe('?')
  })
})
