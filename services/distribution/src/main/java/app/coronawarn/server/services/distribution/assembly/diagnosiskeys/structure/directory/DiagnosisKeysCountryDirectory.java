/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.Export;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator.DateAggregatingDecorator;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator.DateIndexingDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.Collection;
import java.util.Set;

public class DiagnosisKeysCountryDirectory extends IndexDirectoryOnDisk<String> {

  private final Collection<Export> diagnosisKeys;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Constructs a {@link DiagnosisKeysCountryDirectory} instance that represents the {@code .../country/:country/...}
   * portion of the diagnosis key directory structure.
   *
   * @param diagnosisKeys  The diagnosis keys processed in the contained sub directories.
   * @param cryptoProvider The {@link CryptoProvider} used for payload signing.
   */
  public DiagnosisKeysCountryDirectory(Collection<Export> diagnosisKeys,
      CryptoProvider cryptoProvider, DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig.getApi().getCountryPath(), __ ->
        Set.of(distributionServiceConfig.getApi().getCountryGermany()), Object::toString);
    this.diagnosisKeys = diagnosisKeys;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritableToAll(__ -> {
      DiagnosisKeysExportBatchDirectory dateDirectory = new DiagnosisKeysExportBatchDirectory(diagnosisKeys,
              cryptoProvider, distributionServiceConfig);
      return decorateDateDirectory(dateDirectory);
    });
    super.prepare(indices);
  }

  private DateAggregatingDecorator decorateDateDirectory(
          DiagnosisKeysExportBatchDirectory dateDirectory) {
    return new DateAggregatingDecorator(new DateIndexingDecorator(dateDirectory, distributionServiceConfig),
        cryptoProvider, distributionServiceConfig);
  }
}