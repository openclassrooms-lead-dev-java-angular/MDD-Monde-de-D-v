import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'truncate',
  standalone: true,
})
export class TruncatePipe implements PipeTransform {
  transform(
    value: string | null | undefined,
    limit: number,
    suffix: string = '...',
  ): string {
    if (!value) {
      return '';
    }

    if (value.length <= limit) {
      return value;
    }

    const truncated = value.slice(0, limit);
    const lastSpaceIndex = truncated.lastIndexOf(' ');

    const result =
      lastSpaceIndex > 0 ? truncated.slice(0, lastSpaceIndex) : truncated;

    return result + suffix;
  }
}
