# NRSDownloader

NRSDownloader is an Android based program that aims at accelerating network transmission speed on mobile device by bandwidth aggregation on application layer.
Current WLAN transmission is based on Wi-Fi Direct.

## Installation

Use Android Studio or some other IDE to build the project.

## Usage

0. Wi-Fi interface should be enabled on each end
1. Launch the master end in your phone
2. Launch the slave end in the phones you want them to assist the master
3. Available slaves can be shown in master end, choose the slaves that you want to employ in the master to send connection request to them by click the name of slave on the screen
4. After slaves accept the request, Wi-Fi Direct connection is built
5. Click "Listen" button to make slave listen
6. Key in the URL of the file you want to download in master, and click "RemoteDownload" to start the collaborative downloading

## Contributing

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request :D

## History

08/18/2015: First version released. Support collaborative downloading by employing multiple slaves to gain a high entire bandwidth.
