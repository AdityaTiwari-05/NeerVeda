import { getStatusColor, getParameterStatus, formatNumber, formatPercentage, thresholds } from "./utils";

describe("getStatusColor", () => {
  it("returns green classes for SAFE", () => {
    expect(getStatusColor("SAFE")).toContain("green");
  });
  it("returns amber classes for WARNING", () => {
    expect(getStatusColor("WARNING")).toContain("amber");
  });
  it("returns red classes for DANGER", () => {
    expect(getStatusColor("DANGER")).toContain("red");
  });
});

describe("getParameterStatus", () => {
  it("returns SAFE for normal pH", () => {
    expect(getParameterStatus("ph", 7.2)).toBe("SAFE");
  });
  it("returns DANGER for low pH", () => {
    expect(getParameterStatus("ph", 5.8)).toBe("DANGER");
  });
  it("returns DANGER for high pH", () => {
    expect(getParameterStatus("ph", 9.5)).toBe("DANGER");
  });
  it("returns SAFE for normal TDS", () => {
    expect(getParameterStatus("tds", 300)).toBe("SAFE");
  });
  it("returns DANGER for high TDS", () => {
    expect(getParameterStatus("tds", 750)).toBe("DANGER");
  });
  it("returns SAFE for safe turbidity", () => {
    expect(getParameterStatus("turbidity", 2.0)).toBe("SAFE");
  });
  it("returns DANGER for high turbidity", () => {
    expect(getParameterStatus("turbidity", 8.0)).toBe("DANGER");
  });
});

describe("formatNumber", () => {
  it("formats with 1 decimal by default", () => {
    expect(formatNumber(7.234)).toBe("7.2");
  });
  it("formats with specified decimals", () => {
    expect(formatNumber(7.234, 2)).toBe("7.23");
  });
});

describe("formatPercentage", () => {
  it("converts 0.75 to 75.0%", () => {
    expect(formatPercentage(0.75)).toBe("75.0%");
  });
  it("converts 0 to 0.0%", () => {
    expect(formatPercentage(0)).toBe("0.0%");
  });
});

describe("thresholds", () => {
  it("pH max is 8.5", () => {
    expect(thresholds.ph.max).toBe(8.5);
  });
  it("TDS max is 500", () => {
    expect(thresholds.tds.max).toBe(500);
  });
  it("turbidity max is 5", () => {
    expect(thresholds.turbidity.max).toBe(5);
  });
});
