export function computeCardWidth (width: number, breakpoint: string, cardPadding: number = 16): number {
  switch (breakpoint) {
    case 'xs':
      // Each card has `mx-2 my-2` (8px on each side) → 32px total horizontal margin per row.
      // Floor to avoid sub-pixel widths pushing the second card onto a new line.
      return Math.floor((width - (cardPadding * 4)) / 2)
    default:
      return 150
  }
}
