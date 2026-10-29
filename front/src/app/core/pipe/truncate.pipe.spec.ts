import { TruncatePipe } from './truncate.pipe';

describe('TruncatePipe', () => {
  let pipe: TruncatePipe;

  beforeEach(() => {
    pipe = new TruncatePipe();
  });

  it('should create the pipe', () => {
    expect(pipe).toBeTruthy();
  });

  it('should return an empty string when the value is an empty string', () => {
    expect(pipe.transform('', 5)).toBe('');
  });

  it('should return an empty string when the value is null', () => {
    expect(pipe.transform(null, 5)).toBe('');
  });

  it('should return an empty string when the value is undefined', () => {
    expect(pipe.transform(undefined, 5)).toBe('');
  });

  it('should preserve the value when its length is less than the limit', () => {
    expect(pipe.transform('Hello', 10)).toBe('Hello');
  });

  it('should preserve the value when its length equals the limit', () => {
    expect(pipe.transform('Hello', 5)).toBe('Hello');
  });

  it('should truncate the value when its length exceeds the limit', () => {
    expect(pipe.transform('Hello world', 5)).toBe('Hello...');
  });

  it('should truncate at the last space before the limit', () => {
    expect(pipe.transform('Hello world', 8)).toBe('Hello...');
  });

  it('should use the default suffix when no suffix is provided', () => {
    expect(pipe.transform('Hello world', 5)).toBe('Hello...');
  });

  it('should use a custom suffix', () => {
    expect(pipe.transform('Hello world', 5, '…')).toBe('Hello…');
  });

  it('should preserve the truncated portion when there is no space', () => {
    expect(pipe.transform('HelloWorld', 5)).toBe('Hello...');
  });

  it('should preserve the truncated portion when there is no space before the limit', () => {
    expect(pipe.transform('Hello world', 3)).toBe('Hel...');
  });

  it('should return the suffix when the limit is zero and the value is not empty', () => {
    expect(pipe.transform('Hello', 0)).toBe('...');
  });

  it('should return the original value when the limit exceeds its length', () => {
    expect(pipe.transform('Short article content.', 100)).toBe(
      'Short article content.',
    );
  });

  it('should preserve special characters when the value is shorter than the limit', () => {
    const content = '&lt;script&gt;alert("test")&lt;/script&gt; & Angular';

    expect(pipe.transform(content, 100)).toBe(content);
  });
});
