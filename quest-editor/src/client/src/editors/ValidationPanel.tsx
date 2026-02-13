import React, { useState } from 'react';
import { api } from '../api';

export default function ValidationPanel() {
  const [result, setResult] = useState<{ errors: string[]; warnings: string[] } | null>(null);
  const [loading, setLoading] = useState(false);

  const runValidation = async () => {
    setLoading(true);
    try {
      const res = await api.validate();
      setResult(res);
    } catch (e) {
      setResult({ errors: ['Failed to connect to server'], warnings: [] });
    }
    setLoading(false);
  };

  return (
    <div>
      <h2>Content Validation</h2>
      <div className="panel">
        <button onClick={runValidation} disabled={loading}>
          {loading ? 'Validating...' : 'Run Validation'}
        </button>

        {result && (
          <div style={{ marginTop: 20 }}>
            {result.errors.length === 0 && result.warnings.length === 0 ? (
              <p className="validation-ok">All content is valid!</p>
            ) : (
              <>
                {result.errors.length > 0 && (
                  <div>
                    <h3 style={{ color: '#dc3545' }}>Errors ({result.errors.length})</h3>
                    {result.errors.map((e, i) => (
                      <p key={i} className="validation-error">{e}</p>
                    ))}
                  </div>
                )}
                {result.warnings.length > 0 && (
                  <div style={{ marginTop: 15 }}>
                    <h3 style={{ color: '#ffc107' }}>Warnings ({result.warnings.length})</h3>
                    {result.warnings.map((w, i) => (
                      <p key={i} className="validation-warning">{w}</p>
                    ))}
                  </div>
                )}
              </>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
